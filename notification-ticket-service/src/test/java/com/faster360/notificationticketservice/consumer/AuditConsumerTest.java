package com.faster360.notificationticketservice.consumer;

import com.faster360.notificationticketservice.consumer.audit.AuditConsumer;
import com.faster360.notificationticketservice.dto.OrderCreatedEvent;
import com.rabbitmq.client.Channel;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class AuditConsumerTest {

    private final AuditConsumer consumer = new AuditConsumer();

    private OrderCreatedEvent event(String amount) {
        OrderCreatedEvent e = new OrderCreatedEvent();
        e.setOrderNumber("ORD-1");
        e.setCustomerName("Cliente");
        e.setCustomerEmail("cliente@test.com");
        e.setTotalAmount(new BigDecimal(amount));
        return e;
    }

    @Test
    void pedidoNormalSeConfirmaConAck() throws Exception {
        Channel channel = mock(Channel.class);
        consumer.registerAudit(event("15990.00"), channel, 1L);
        verify(channel).basicAck(1L, false);
        verify(channel, never()).basicNack(1L, false, false);
    }

    @Test
    void montoQueFallaVaALaDlqConNackSinReencolar() throws Exception {
        Channel channel = mock(Channel.class);
        consumer.registerAudit(event("777.00"), channel, 2L);
        verify(channel).basicNack(2L, false, false);
        verify(channel, never()).basicAck(2L, false);
    }
}
