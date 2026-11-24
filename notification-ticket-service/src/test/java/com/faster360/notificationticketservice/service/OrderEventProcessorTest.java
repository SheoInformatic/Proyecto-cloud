package com.faster360.notificationticketservice.service;

import com.faster360.notificationticketservice.dto.OrderAnalyticsEvent;
import com.faster360.notificationticketservice.dto.OrderCreatedEvent;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderEventProcessorTest {

    private final OrderEventProcessor processor = new OrderEventProcessor();

    private OrderCreatedEvent event(String number, String email, String amount) {
        OrderCreatedEvent e = new OrderCreatedEvent();
        e.setOrderNumber(number);
        e.setCustomerName("Cliente");
        e.setCustomerEmail(email);
        e.setTotalAmount(amount == null ? null : new BigDecimal(amount));
        return e;
    }

    @Test
    void processesValidEvent() {
        assertDoesNotThrow(() -> processor.process(event("A-1", "a@b.cl", "100.00")));
    }

    @Test
    void rejectsPayloadWithoutOrderNumberAsNonRetryable() {
        assertThrows(InvalidEventException.class, () -> processor.process(event(" ", "a@b.cl", "100")));
    }

    @Test
    void rejectsInvalidAmount() {
        assertThrows(InvalidEventException.class, () -> processor.process(event("A-2", "a@b.cl", "-5")));
        assertThrows(InvalidEventException.class, () -> processor.process(event("A-3", "a@b.cl", null)));
    }

    @Test
    void simulatedTransientFailureIsRetryableNotInvalid() {
        assertThrows(IllegalStateException.class,
                () -> processor.process(event("A-4", "fail@faster360.com", "100")));
    }

    @Test
    void aggregatorAccumulatesOrdersAndRevenue() {
        OrderAnalyticsAggregator agg = new OrderAnalyticsAggregator();
        for (int i = 0; i < 2; i++) {
            OrderAnalyticsEvent e = new OrderAnalyticsEvent();
            e.setOrderNumber("O-" + i);
            e.setTotalAmount(new BigDecimal("50.00"));
            e.setStatus("CREATED");
            e.setCustomerEmailDomain("gmail.com");
            agg.record(e);
        }
        assertEquals(2L, agg.summary().get("totalOrders"));
        assertEquals(new BigDecimal("100.00"), agg.summary().get("totalRevenue"));
    }

    @Test
    void aggregatorRejectsInvalidEvent() {
        assertThrows(InvalidEventException.class, () -> new OrderAnalyticsAggregator().record(new OrderAnalyticsEvent()));
    }
}
