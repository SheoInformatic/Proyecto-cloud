package com.faster360.notificationticketservice.service;

import com.faster360.notificationticketservice.dto.OrderCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Lógica de dominio del flujo de eventos de órdenes (sin dependencias de mensajería):
 * valida el evento y registra la confirmación. Los listeners solo delegan aquí.
 */
@Service
public class OrderEventProcessor {

    private static final Logger log = LoggerFactory.getLogger(OrderEventProcessor.class);

    public void process(OrderCreatedEvent event) {
        validate(event);

        // Regla de simulación de fallo transitorio (se reintenta y termina en el DLT)
        if ("fail@faster360.com".equalsIgnoreCase(event.getCustomerEmail())) {
            throw new IllegalStateException("Servicio de confirmación no disponible para " + event.getCustomerEmail());
        }

        log.info("[DOMINIO ÓRDENES] Orden {} confirmada para {} por ${}",
                event.getOrderNumber(), event.getCustomerName(), event.getTotalAmount());
    }

    void validate(OrderCreatedEvent event) {
        if (event == null) {
            throw new InvalidEventException("Evento nulo");
        }
        if (event.getOrderNumber() == null || event.getOrderNumber().isBlank()) {
            throw new InvalidEventException("El evento no contiene número de orden");
        }
        if (event.getCustomerEmail() == null || !event.getCustomerEmail().contains("@")) {
            throw new InvalidEventException("Correo inválido en la orden " + event.getOrderNumber());
        }
        if (event.getTotalAmount() == null || event.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidEventException("Monto inválido en la orden " + event.getOrderNumber());
        }
    }
}
