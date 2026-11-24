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

/**
 * Audita lo que llega a las tres DLQ (notificación, ticket y auditoría).
 *
 * Para la demostración se puede desactivar con faster360.messaging.dlq-audit-enabled=false
 * (variable de entorno DLQ_AUDIT_ENABLED=false): así los mensajes fallidos
 * se acumulan en cada DLQ y se ven en la consola de RabbitMQ.
 */
@Component
public class DeadLetterConsumer {

    private static final Logger log = LoggerFactory.getLogger(DeadLetterConsumer.class);

    @RabbitListener(
            queues = {
                    "${faster360.messaging.queues.notification-dlq}",
                    "${faster360.messaging.queues.ticket-dlq}",
                    "${faster360.messaging.queues.audit-dlq}"
            },
            autoStartup = "${faster360.messaging.dlq-audit-enabled:true}")
    public void processDeadLetter(OrderCreatedEvent event,
                                  Channel channel,
                                  @Header(AmqpHeaders.CONSUMER_QUEUE) String dlqName,
                                  @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        log.warn("================================================================================");
        log.warn("[AUDITORÍA DLQ - MENSAJE NO ENTREGADO / FALLIDO]");
        log.warn("DLQ de origen: {}", dlqName);
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
