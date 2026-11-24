package com.faster360.notificationticketservice.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Evento del tópico faster360.orders.analytics (mismo contrato que publica orderservice). */
public class OrderAnalyticsEvent implements Serializable {

    private String orderNumber;
    private String customerEmailDomain;
    private BigDecimal totalAmount;
    private String status;
    private LocalDateTime createdAt;

    public OrderAnalyticsEvent() {}

    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }

    public String getCustomerEmailDomain() { return customerEmailDomain; }
    public void setCustomerEmailDomain(String customerEmailDomain) { this.customerEmailDomain = customerEmailDomain; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
