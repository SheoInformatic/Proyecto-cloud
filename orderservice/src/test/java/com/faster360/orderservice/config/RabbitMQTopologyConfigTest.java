package com.faster360.orderservice.config;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Verifica la topología de RabbitMQ sin necesitar un broker: tres colas, cada una con su propia DLQ. */
class RabbitMQTopologyConfigTest {

    private static final String DLX = "faster360.orders.dlx";

    private RabbitMQTopologyConfig config() {
        RabbitMQProperties p = new RabbitMQProperties();
        p.setExchange("faster360.orders.exchange");
        p.setDlx(DLX);
        p.getRoutingKeys().setOrderCreated("order.created");
        p.getRoutingKeys().setNotificationDeadLetter("order.notification.dead-letter");
        p.getRoutingKeys().setTicketDeadLetter("order.ticket.dead-letter");
        p.getRoutingKeys().setAuditDeadLetter("order.audit.dead-letter");
        p.getQueues().setNotification("faster360.orders.notification.queue");
        p.getQueues().setTicket("faster360.orders.ticket.queue");
        p.getQueues().setAudit("faster360.orders.audit.queue");
        p.getQueues().setNotificationDlq("faster360.orders.notification.dlq");
        p.getQueues().setTicketDlq("faster360.orders.ticket.dlq");
        p.getQueues().setAuditDlq("faster360.orders.audit.dlq");
        return new RabbitMQTopologyConfig(p);
    }

    private void assertQueueHasOwnDlq(Queue queue, Binding dlqBinding, String expectedKey, String expectedDlq) {
        assertEquals(DLX, queue.getArguments().get("x-dead-letter-exchange"));
        assertEquals(expectedKey, queue.getArguments().get("x-dead-letter-routing-key"));
        assertEquals(expectedDlq, dlqBinding.getDestination());
        assertEquals(DLX, dlqBinding.getExchange());
        assertEquals(expectedKey, dlqBinding.getRoutingKey());
    }

    @Test
    void cadaColaTieneSuPropiaDlq() {
        RabbitMQTopologyConfig c = config();
        assertQueueHasOwnDlq(c.notificationQueue(), c.notificationDlqBinding(),
                "order.notification.dead-letter", "faster360.orders.notification.dlq");
        assertQueueHasOwnDlq(c.ticketQueue(), c.ticketDlqBinding(),
                "order.ticket.dead-letter", "faster360.orders.ticket.dlq");
        assertQueueHasOwnDlq(c.auditQueue(), c.auditDlqBinding(),
                "order.audit.dead-letter", "faster360.orders.audit.dlq");
    }

    @Test
    void lasTresColasUsanClavesDeDeadLetterDistintas() {
        RabbitMQTopologyConfig c = config();
        Set<Object> keys = new HashSet<>();
        keys.add(c.notificationQueue().getArguments().get("x-dead-letter-routing-key"));
        keys.add(c.ticketQueue().getArguments().get("x-dead-letter-routing-key"));
        keys.add(c.auditQueue().getArguments().get("x-dead-letter-routing-key"));
        assertEquals(3, keys.size());
    }

    @Test
    void lasTresColasRecibenLosPedidosDelExchangeTopic() {
        RabbitMQTopologyConfig c = config();
        for (Binding b : new Binding[]{c.notificationBinding(), c.ticketBinding(), c.auditBinding()}) {
            assertEquals("faster360.orders.exchange", b.getExchange());
            assertEquals("order.created", b.getRoutingKey());
        }
        assertTrue(c.orderExchange().getType().equals("topic"));
        assertTrue(c.deadLetterExchange().getType().equals("direct"));
    }
}
