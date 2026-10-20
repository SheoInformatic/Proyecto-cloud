package com.faster360.notificationticketservice.consumer.dlq;

import com.faster360.notificationticketservice.dto.OrderCreatedEvent;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class DeadLetterConsumer {

    private static final Logger log = LoggerFactory.getLogger(DeadLetterConsumer.class);

    @RabbitListener(queues = "${faster360.messaging.queues.dlq}")
    public void processDeadLetter(OrderCreatedEvent event,
                                  Channel channel,
                                  @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        log.warn("================================================================================");
        log.warn("[AUDITORÍA DLQ - MENSAJE NO ENTREGADO / FALLIDO]");
        log.warn("DeliveryTag DLQ: {}", deliveryTag);
        log.warn("Orden N°: {}", event.getOrderNumber());
        log.warn("Cliente: {} ({})", event.getCustomerName(), event.getCustomerEmail());
        log.warn("Monto: ${}", event.getTotalAmount());
        log.warn("Acción: Registrado en log de auditoría para diagnóstico e investigación operativa.");
        log.warn("================================================================================");

        // Confirmamos el procesamiento del mensaje en la DLQ para que quede auditado y no colapse la cola
        channel.basicAck(deliveryTag, false);
    }
}
