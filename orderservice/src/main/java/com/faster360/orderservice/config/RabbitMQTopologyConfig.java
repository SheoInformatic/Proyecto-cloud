package com.faster360.orderservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitMQTopologyConfig {

    private final RabbitMQProperties properties;

    public RabbitMQTopologyConfig(RabbitMQProperties properties) {
        this.properties = properties;
    }

    // 1. Exchanges
    @Bean
    public TopicExchange orderExchange() {
        return new TopicExchange(properties.getExchange(), true, false);
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(properties.getDlx(), true, false);
    }

    // 2. Dead Letter Queue (DLQ)
    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(properties.getQueues().getDlq()).build();
    }

    @Bean
    public Binding dlqBinding() {
        return BindingBuilder.bind(deadLetterQueue())
                .to(deadLetterExchange())
                .with(properties.getRoutingKeys().getDeadLetter());
    }

    // 3. Cola de Notificaciones configurada con DLQ
    @Bean
    public Queue notificationQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", properties.getDlx());
        args.put("x-dead-letter-routing-key", properties.getRoutingKeys().getDeadLetter());
        return new Queue(properties.getQueues().getNotification(), true, false, false, args);
    }

    @Bean
    public Binding notificationBinding() {
        return BindingBuilder.bind(notificationQueue())
                .to(orderExchange())
                .with(properties.getRoutingKeys().getOrderCreated());
    }

    // 4. Cola de Emisión de Tickets configurada con DLQ
    @Bean
    public Queue ticketQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", properties.getDlx());
        args.put("x-dead-letter-routing-key", properties.getRoutingKeys().getDeadLetter());
        return new Queue(properties.getQueues().getTicket(), true, false, false, args);
    }

    @Bean
    public Binding ticketBinding() {
        return BindingBuilder.bind(ticketQueue())
                .to(orderExchange())
                .with(properties.getRoutingKeys().getOrderCreated());
    }

    // 5. Conversor JSON para serializar/deserializar mensajes
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}