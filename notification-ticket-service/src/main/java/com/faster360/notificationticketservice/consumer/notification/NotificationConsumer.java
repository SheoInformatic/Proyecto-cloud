package com.faster360.notificationticketservice.consumer.notification;

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
public class NotificationConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificationConsumer.class);

    @RabbitListener(queues = "${faster360.messaging.queues.notification}")
    public void processNotification(OrderCreatedEvent event,
                                    Channel channel,
                                    @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        log.info("[DOMINIO NOTIFICACIÓN] Mensaje recibido. DeliveryTag: {}, Orden: {}", deliveryTag, event.getOrderNumber());

        try {
            // Regla de simulación de fallo para pruebas:
            // Si el correo es "fail@faster360.com", forzamos un error para demostrar la DLQ
            if ("fail@faster360.com".equalsIgnoreCase(event.getCustomerEmail())) {
                throw new IllegalArgumentException("Dirección de correo no permitida para notificaciones automáticas.");
            }

            // Lógica de Negocio: Envío de correo o notificación
            log.info("Enviando correo de confirmación a: {} para la orden: {}", event.getCustomerEmail(), event.getOrderNumber());
            log.info("Notificación enviada exitosamente al cliente {}", event.getCustomerName());

            // 1. Confirmación de Éxito (ACK manual explícito)
            channel.basicAck(deliveryTag, false);
            log.info("[DOMINIO NOTIFICACIÓN] ACK confirmado para orden: {}", event.getOrderNumber());

        } catch (Exception ex) {
            // 2. Manejo Explícito de Fallo: Log detallado y desvío a DLQ
            log.error("[DOMINIO NOTIFICACIÓN - ERROR] Fallo al procesar la notificación de la orden {}. Causa: {}",
                    event.getOrderNumber(), ex.getMessage());

            /*
             * basicNack(deliveryTag, multiple, requeue)
             * - requeue = false: Hace que RabbitMQ NO vuelva a meter el mensaje a la cola principal,
             *   sino que lo enrute de inmediato a la Dead Letter Queue (DLQ) vía x-dead-letter-exchange.
             */
            channel.basicNack(deliveryTag, false, false);
            log.warn("[DOMINIO NOTIFICACIÓN] Mensaje rechazado con NACK (requeue=false). Enviado a la DLQ.");
        }
    }
}