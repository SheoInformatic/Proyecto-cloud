package com.faster360.orderservice.messaging;

import com.faster360.orderservice.config.KafkaTopicsProperties;
import com.faster360.orderservice.domain.Order;
import com.faster360.orderservice.dto.OrderAnalyticsEvent;
import com.faster360.orderservice.dto.OrderCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Productor Kafka del dominio de órdenes. Publica en dos tópicos usando el número de
 * orden como clave (todos los eventos de una orden caen en la misma partición y se
 * procesan en orden). Un fallo de Kafka NO revierte la orden ni el flujo RabbitMQ:
 * se registra en logs.
 */
@Component
public class OrderKafkaPublisher {

    private static final Logger log = LoggerFactory.getLogger(OrderKafkaPublisher.class);

    private final KafkaTemplate<String, OrderCreatedEvent> eventTemplate;
    private final KafkaTemplate<String, OrderAnalyticsEvent> analyticsTemplate;
    private final KafkaTopicsProperties topics;

    public OrderKafkaPublisher(KafkaTemplate<String, OrderCreatedEvent> orderEventKafkaTemplate,
                               KafkaTemplate<String, OrderAnalyticsEvent> orderAnalyticsKafkaTemplate,
                               KafkaTopicsProperties topics) {
        this.eventTemplate = orderEventKafkaTemplate;
        this.analyticsTemplate = orderAnalyticsKafkaTemplate;
        this.topics = topics;
    }

    public void publishOrderCreated(Order order, OrderCreatedEvent event) {
        send(topics.events(), order.getOrderNumber(), event, eventTemplate);

        OrderAnalyticsEvent analytics = new OrderAnalyticsEvent(
                order.getOrderNumber(),
                emailDomain(order.getCustomerEmail()),
                order.getTotalAmount(),
                order.getStatus().name(),
                order.getCreatedAt());
        send(topics.analytics(), order.getOrderNumber(), analytics, analyticsTemplate);
    }

    private <T> void send(String topic, String key, T payload, KafkaTemplate<String, T> template) {
        try {
            template.send(topic, key, payload).whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("[KAFKA PRODUCER] Falló el envío a '{}' (clave {}): {}", topic, key, ex.getMessage());
                } else {
                    var meta = result.getRecordMetadata();
                    log.info("[KAFKA PRODUCER] Evento enviado a '{}' partición {} offset {} (clave {})",
                            meta.topic(), meta.partition(), meta.offset(), key);
                }
            });
        } catch (Exception ex) {
            // send() puede lanzar de forma síncrona (p.ej. timeout obteniendo metadatos)
            log.error("[KAFKA PRODUCER] No se pudo publicar en '{}' (clave {}): {}", topic, key, ex.getMessage());
        }
    }

    private String emailDomain(String email) {
        int at = email == null ? -1 : email.lastIndexOf('@');
        return at >= 0 ? email.substring(at + 1).toLowerCase() : "desconocido";
    }
}
