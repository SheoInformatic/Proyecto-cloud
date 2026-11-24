package com.faster360.orderservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * Topología de RabbitMQ: un exchange topic para los pedidos, un exchange direct para los
 * mensajes fallidos (DLX) y tres colas de trabajo (notificación, ticket y auditoría),
 * cada una con su propia Dead Letter Queue.
 */
@Configuration
public class RabbitMQTopologyConfig {

    private final RabbitMQProperties properties;

    public RabbitMQTopologyConfig(RabbitMQProperties properties) {
        this.properties = properties;
    }

    // 1. Exchanges: topic para pedidos, direct para dead-letter
    @Bean
    public TopicExchange orderExchange() {
        return new TopicExchange(properties.getExchange(), true, false);
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(properties.getDlx(), true, false);
    }

    // 2. Una cola de trabajo por responsabilidad, cada una apuntando a su DLQ mediante su routing key
    private Queue workQueue(String name, String deadLetterRoutingKey) {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", properties.getDlx());
        args.put("x-dead-letter-routing-key", deadLetterRoutingKey);
        return new Queue(name, true, false, false, args);
    }

    private Binding orderBinding(Queue queue) {
        return BindingBuilder.bind(queue)
                .to(orderExchange())
                .with(properties.getRoutingKeys().getOrderCreated());
    }

    private Binding dlqBinding(Queue dlq, String deadLetterRoutingKey) {
        return BindingBuilder.bind(dlq)
                .to(deadLetterExchange())
                .with(deadLetterRoutingKey);
    }

    // Cola de Notificaciones + su DLQ
    @Bean
    public Queue notificationQueue() {
        return workQueue(properties.getQueues().getNotification(), properties.getRoutingKeys().getNotificationDeadLetter());
    }

    @Bean
    public Queue notificationDlq() {
        return QueueBuilder.durable(properties.getQueues().getNotificationDlq()).build();
    }

    @Bean
    public Binding notificationBinding() {
        return orderBinding(notificationQueue());
    }

    @Bean
    public Binding notificationDlqBinding() {
        return dlqBinding(notificationDlq(), properties.getRoutingKeys().getNotificationDeadLetter());
    }

    // Cola de Emisión de Tickets + su DLQ
    @Bean
    public Queue ticketQueue() {
        return workQueue(properties.getQueues().getTicket(), properties.getRoutingKeys().getTicketDeadLetter());
    }

    @Bean
    public Queue ticketDlq() {
        return QueueBuilder.durable(properties.getQueues().getTicketDlq()).build();
    }

    @Bean
    public Binding ticketBinding() {
        return orderBinding(ticketQueue());
    }

    @Bean
    public Binding ticketDlqBinding() {
        return dlqBinding(ticketDlq(), properties.getRoutingKeys().getTicketDeadLetter());
    }

    // Cola de Auditoría + su DLQ
    @Bean
    public Queue auditQueue() {
        return workQueue(properties.getQueues().getAudit(), properties.getRoutingKeys().getAuditDeadLetter());
    }

    @Bean
    public Queue auditDlq() {
        return QueueBuilder.durable(properties.getQueues().getAuditDlq()).build();
    }

    @Bean
    public Binding auditBinding() {
        return orderBinding(auditQueue());
    }

    @Bean
    public Binding auditDlqBinding() {
        return dlqBinding(auditDlq(), properties.getRoutingKeys().getAuditDeadLetter());
    }

    // 3. Conversor JSON para serializar/deserializar mensajes con soporte para Java 8 Date/Time
    @Bean
    public MessageConverter jsonMessageConverter() {
        com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
        objectMapper.findAndRegisterModules();
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(org.springframework.amqp.rabbit.connection.ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }
}
