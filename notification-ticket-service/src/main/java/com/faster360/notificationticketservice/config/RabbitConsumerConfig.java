package com.faster360.notificationticketservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Declara (de forma idempotente, igual que orderservice) las tres colas de trabajo y la
 * Dead Letter Queue propia de cada una.
 */
@Configuration
public class RabbitConsumerConfig {

    @Value("${faster360.messaging.exchange}")
    private String ordersExchange;

    @Value("${faster360.messaging.dlx}")
    private String dlxExchange;

    @Value("${faster360.messaging.queues.notification}")
    private String notificationQueue;

    @Value("${faster360.messaging.queues.ticket}")
    private String ticketQueue;

    @Value("${faster360.messaging.queues.audit}")
    private String auditQueue;

    @Value("${faster360.messaging.queues.notification-dlq}")
    private String notificationDlq;

    @Value("${faster360.messaging.queues.ticket-dlq}")
    private String ticketDlq;

    @Value("${faster360.messaging.queues.audit-dlq}")
    private String auditDlq;

    @Value("${faster360.messaging.routing-keys.order-created}")
    private String orderCreatedRoutingKey;

    @Value("${faster360.messaging.routing-keys.notification-dead-letter}")
    private String notificationDeadLetterKey;

    @Value("${faster360.messaging.routing-keys.ticket-dead-letter}")
    private String ticketDeadLetterKey;

    @Value("${faster360.messaging.routing-keys.audit-dead-letter}")
    private String auditDeadLetterKey;

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        RabbitAdmin admin = new RabbitAdmin(connectionFactory);
        admin.setAutoStartup(true);
        return admin;
    }

    @Bean
    public DirectExchange dlxExchange() {
        return new DirectExchange(dlxExchange, true, false);
    }

    @Bean
    public TopicExchange ordersExchange() {
        return new TopicExchange(ordersExchange, true, false);
    }

    private Queue workQueue(String name, String deadLetterKey) {
        return QueueBuilder.durable(name)
                .withArgument("x-dead-letter-exchange", dlxExchange)
                .withArgument("x-dead-letter-routing-key", deadLetterKey)
                .build();
    }

    private Binding orderBinding(Queue queue) {
        return BindingBuilder.bind(queue).to(ordersExchange()).with(orderCreatedRoutingKey);
    }

    private Binding dlqBinding(Queue dlq, String deadLetterKey) {
        return BindingBuilder.bind(dlq).to(dlxExchange()).with(deadLetterKey);
    }

    // Notificación + su DLQ
    @Bean
    public Queue notificationQueue() { return workQueue(notificationQueue, notificationDeadLetterKey); }

    @Bean
    public Queue notificationDlq() { return QueueBuilder.durable(notificationDlq).build(); }

    @Bean
    public Binding notificationBinding() { return orderBinding(notificationQueue()); }

    @Bean
    public Binding notificationDlqBinding() { return dlqBinding(notificationDlq(), notificationDeadLetterKey); }

    // Ticket + su DLQ
    @Bean
    public Queue ticketQueue() { return workQueue(ticketQueue, ticketDeadLetterKey); }

    @Bean
    public Queue ticketDlq() { return QueueBuilder.durable(ticketDlq).build(); }

    @Bean
    public Binding ticketBinding() { return orderBinding(ticketQueue()); }

    @Bean
    public Binding ticketDlqBinding() { return dlqBinding(ticketDlq(), ticketDeadLetterKey); }

    // Auditoría + su DLQ
    @Bean
    public Queue auditQueue() { return workQueue(auditQueue, auditDeadLetterKey); }

    @Bean
    public Queue auditDlq() { return QueueBuilder.durable(auditDlq).build(); }

    @Bean
    public Binding auditBinding() { return orderBinding(auditQueue()); }

    @Bean
    public Binding auditDlqBinding() { return dlqBinding(auditDlq(), auditDeadLetterKey); }

    @Bean
    public MessageConverter jsonMessageConverter() {
        com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
        objectMapper.findAndRegisterModules();
        return new Jackson2JsonMessageConverter(objectMapper);
    }
}
