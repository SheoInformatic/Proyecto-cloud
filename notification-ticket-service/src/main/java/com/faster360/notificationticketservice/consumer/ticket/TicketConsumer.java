package com.faster360.notificationticketservice.consumer.ticket;

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

@Component
public class TicketConsumer {

    private static final Logger log = LoggerFactory.getLogger(TicketConsumer.class);

    @RabbitListener(queues = "${faster360.messaging.queues.ticket}")
    public void generateTicket(OrderCreatedEvent event,
                               Channel channel,
                               @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        log.info("[DOMINIO TICKET] Mensaje recibido. DeliveryTag: {}, Orden: {}", deliveryTag, event.getOrderNumber());

        try {
            // Regla de simulación de fallo para pruebas:
            // Si el monto total es exactamente 999.00, forzamos un fallo de emisión de ticket
            if (event.getTotalAmount() != null && event.getTotalAmount().compareTo(new BigDecimal("999.00")) == 0) {
                throw new IllegalStateException("Error al generar el comprobante PDF: Servicio de firma no disponible.");
            }

            // Lógica de Negocio: Generación de Ticket / Comprobante
            log.info("Generando comprobante digital para la compra N°: {}", event.getOrderNumber());
            log.info("Ticket generado correctamente por un valor de: ${}", event.getTotalAmount());

            // 1. Confirmación de Éxito (ACK manual explícito)
            channel.basicAck(deliveryTag, false);
            log.info("[DOMINIO TICKET] ACK confirmado para orden: {}", event.getOrderNumber());

        } catch (Exception ex) {
            // 2. Manejo Explícito de Fallo: Log detallado y desvío a DLQ
            log.error("[DOMINIO TICKET - ERROR] No fue posible emitir el ticket de la orden {}. Causa: {}",
                    event.getOrderNumber(), ex.getMessage());

            // NACK sin re-encolado -> Va directo a la DLQ
            channel.basicNack(deliveryTag, false, false);
            log.warn("[DOMINIO TICKET] Mensaje rechazado con NACK (requeue=false). Redirigido a la DLQ para auditoría.");
        }
    }
}