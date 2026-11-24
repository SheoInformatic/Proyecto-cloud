package com.faster360.notificationticketservice.consumer.audit;

import com.faster360.notificationticketservice.dto.OrderCreatedEvent;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;

/**
 * Tercera cola de trabajo: registra cada pedido en un registro de auditoría.
 * Si falla, el mensaje va a su propia DLQ (faster360.orders.audit.dlq).
 */
@Component
public class AuditConsumer {

    private static final Logger log = LoggerFactory.getLogger(AuditConsumer.class);

    /** Monto que fuerza un fallo de auditoría para demostrar la DLQ de esta cola. */
    static final BigDecimal AMOUNT_THAT_FAILS = new BigDecimal("777.00");

    @RabbitListener(queues = "${faster360.messaging.queues.audit}")
    public void registerAudit(OrderCreatedEvent event,
                              Channel channel,
                              @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        log.info("[DOMINIO AUDITORÍA] Mensaje recibido. DeliveryTag: {}, Orden: {}", deliveryTag, event.getOrderNumber());
        try {
            // Regla de simulación de fallo para pruebas: monto exacto de 777.00
            if (event.getTotalAmount() != null && event.getTotalAmount().compareTo(AMOUNT_THAT_FAILS) == 0) {
                throw new IllegalStateException("Registro de auditoría no disponible.");
            }

            log.info("Registro de auditoría guardado: orden {} de {} por ${}",
                    event.getOrderNumber(), event.getCustomerEmail(), event.getTotalAmount());
            channel.basicAck(deliveryTag, false);
            log.info("[DOMINIO AUDITORÍA] ACK confirmado para orden: {}", event.getOrderNumber());
        } catch (Exception ex) {
            log.error("[DOMINIO AUDITORÍA - ERROR] No fue posible auditar la orden {}. Causa: {}",
                    event.getOrderNumber(), ex.getMessage());
            // NACK sin re-encolado -> va directo a la DLQ de auditoría
            channel.basicNack(deliveryTag, false, false);
            log.warn("[DOMINIO AUDITORÍA] Mensaje rechazado con NACK (requeue=false). Enviado a su DLQ.");
        }
    }
}
