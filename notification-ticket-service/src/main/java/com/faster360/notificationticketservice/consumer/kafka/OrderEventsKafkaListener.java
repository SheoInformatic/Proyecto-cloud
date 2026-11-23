package com.faster360.notificationticketservice.consumer.kafka;

import com.faster360.notificationticketservice.dto.OrderCreatedEvent;
import com.faster360.notificationticketservice.service.OrderEventProcessor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Consumidor del tópico de eventos de órdenes. Solo adapta el mensaje de Kafka y delega
 * en el servicio de dominio. Si el servicio lanza una excepción, el DefaultErrorHandler
 * aplica reintentos y finalmente envía el registro al Dead Letter Topic.
 */
@Component
public class OrderEventsKafkaListener {

    private static final Logger log = LoggerFactory.getLogger(OrderEventsKafkaListener.class);

    private final OrderEventProcessor processor;

    public OrderEventsKafkaListener(OrderEventProcessor processor) {
        this.processor = processor;
    }

    @KafkaListener(
            topics = "${faster360.kafka.topics.events}",
            groupId = "${faster360.kafka.groups.order-events}",
            containerFactory = "orderEventsListenerFactory")
    public void onOrderEvent(ConsumerRecord<String, OrderCreatedEvent> record) {
        log.info("[KAFKA EVENTS] Recibido {}-{}@{} clave={}",
                record.topic(), record.partition(), record.offset(), record.key());
        processor.process(record.value());
    }
}
