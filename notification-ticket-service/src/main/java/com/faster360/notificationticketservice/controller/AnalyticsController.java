package com.faster360.notificationticketservice.controller;

import com.faster360.notificationticketservice.service.OrderAnalyticsAggregator;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** Expone el resumen analítico construido a partir del tópico Kafka de analítica. */
@RestController
@RequestMapping("/api/analytics")
@CrossOrigin(origins = "*")
public class AnalyticsController {

    private final OrderAnalyticsAggregator aggregator;

    public AnalyticsController(OrderAnalyticsAggregator aggregator) {
        this.aggregator = aggregator;
    }

    @GetMapping("/summary")
    public Map<String, Object> summary() {
        return aggregator.summary();
    }
}
