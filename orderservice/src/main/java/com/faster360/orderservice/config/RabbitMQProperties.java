package com.faster360.orderservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "faster360.messaging")
public class RabbitMQProperties {

    private String exchange;
    private String dlx;
    private RoutingKeys routingKeys = new RoutingKeys();
    private Queues queues = new Queues();

    /** Cada cola tiene su propia routing key de dead-letter, para que cada una caiga en su propia DLQ. */
    public static class RoutingKeys {
        private String orderCreated;
        private String notificationDeadLetter;
        private String ticketDeadLetter;
        private String auditDeadLetter;

        public String getOrderCreated() { return orderCreated; }
        public void setOrderCreated(String orderCreated) { this.orderCreated = orderCreated; }
        public String getNotificationDeadLetter() { return notificationDeadLetter; }
        public void setNotificationDeadLetter(String notificationDeadLetter) { this.notificationDeadLetter = notificationDeadLetter; }
        public String getTicketDeadLetter() { return ticketDeadLetter; }
        public void setTicketDeadLetter(String ticketDeadLetter) { this.ticketDeadLetter = ticketDeadLetter; }
        public String getAuditDeadLetter() { return auditDeadLetter; }
        public void setAuditDeadLetter(String auditDeadLetter) { this.auditDeadLetter = auditDeadLetter; }
    }

    /** Tres colas de trabajo y la DLQ propia de cada una. */
    public static class Queues {
        private String notification;
        private String ticket;
        private String audit;
        private String notificationDlq;
        private String ticketDlq;
        private String auditDlq;

        public String getNotification() { return notification; }
        public void setNotification(String notification) { this.notification = notification; }
        public String getTicket() { return ticket; }
        public void setTicket(String ticket) { this.ticket = ticket; }
        public String getAudit() { return audit; }
        public void setAudit(String audit) { this.audit = audit; }
        public String getNotificationDlq() { return notificationDlq; }
        public void setNotificationDlq(String notificationDlq) { this.notificationDlq = notificationDlq; }
        public String getTicketDlq() { return ticketDlq; }
        public void setTicketDlq(String ticketDlq) { this.ticketDlq = ticketDlq; }
        public String getAuditDlq() { return auditDlq; }
        public void setAuditDlq(String auditDlq) { this.auditDlq = auditDlq; }
    }

    public String getExchange() { return exchange; }
    public void setExchange(String exchange) { this.exchange = exchange; }
    public String getDlx() { return dlx; }
    public void setDlx(String dlx) { this.dlx = dlx; }
    public RoutingKeys getRoutingKeys() { return routingKeys; }
    public void setRoutingKeys(RoutingKeys routingKeys) { this.routingKeys = routingKeys; }
    public Queues getQueues() { return queues; }
    public void setQueues(Queues queues) { this.queues = queues; }
}
