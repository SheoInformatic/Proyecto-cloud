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

    public static class RoutingKeys {
        private String orderCreated;
        private String deadLetter;

        public String getOrderCreated() { return orderCreated; }
        public void setOrderCreated(String orderCreated) { this.orderCreated = orderCreated; }
        public String getDeadLetter() { return deadLetter; }
        public void setDeadLetter(String deadLetter) { this.deadLetter = deadLetter; }
    }

    public static class Queues {
        private String notification;
        private String ticket;
        private String dlq;

        public String getNotification() { return notification; }
        public void setNotification(String notification) { this.notification = notification; }
        public String getTicket() { return ticket; }
        public void setTicket(String ticket) { this.ticket = ticket; }
        public String getDlq() { return dlq; }
        public void setDlq(String dlq) { this.dlq = dlq; }
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