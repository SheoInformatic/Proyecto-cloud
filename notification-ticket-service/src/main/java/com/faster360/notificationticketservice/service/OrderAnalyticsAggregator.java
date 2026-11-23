package com.faster360.notificationticketservice.service;

import com.faster360.notificationticketservice.dto.OrderAnalyticsEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

/** Agregación analítica en memoria (órdenes, ingresos, por estado y por dominio de correo). */
@Service
public class OrderAnalyticsAggregator {

    private static final Logger log = LoggerFactory.getLogger(OrderAnalyticsAggregator.class);

    private final LongAdder totalOrders = new LongAdder();
    private BigDecimal totalRevenue = BigDecimal.ZERO;
    private final Map<String, LongAdder> byStatus = new ConcurrentHashMap<>();
    private final Map<String, LongAdder> byEmailDomain = new ConcurrentHashMap<>();

    public void record(OrderAnalyticsEvent event) {
        if (event == null || event.getOrderNumber() == null || event.getTotalAmount() == null
                || event.getTotalAmount().signum() < 0) {
            throw new InvalidEventException("Evento analítico inválido: " + (event == null ? "nulo" : event.getOrderNumber()));
        }
        totalOrders.increment();
        addRevenue(event.getTotalAmount());
        byStatus.computeIfAbsent(String.valueOf(event.getStatus()), k -> new LongAdder()).increment();
        byEmailDomain.computeIfAbsent(String.valueOf(event.getCustomerEmailDomain()), k -> new LongAdder()).increment();
        log.info("[ANALÍTICA] Orden {} registrada ({} órdenes acumuladas)", event.getOrderNumber(), totalOrders.sum());
    }

    private synchronized void addRevenue(BigDecimal amount) {
        totalRevenue = totalRevenue.add(amount);
    }

    public synchronized Map<String, Object> summary() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalOrders", totalOrders.sum());
        result.put("totalRevenue", totalRevenue);
        result.put("byStatus", toCounts(byStatus));
        result.put("byEmailDomain", toCounts(byEmailDomain));
        return result;
    }

    private Map<String, Long> toCounts(Map<String, LongAdder> source) {
        Map<String, Long> out = new LinkedHashMap<>();
        source.forEach((k, v) -> out.put(k, v.sum()));
        return out;
    }
}
