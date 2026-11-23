package com.faster360.notificationticketservice.consumer.kafka;

import com.faster360.notificationticketservice.dto.OrderAnalyticsEvent;
import com.faster360.notificationticketservice.service.OrderAnalyticsAggregator;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/** Consumidor del tópico analítico (consumer group independiente del flujo operacional). */
@Component
public class OrderAnalyticsKafkaListener {

    private static final Logger log = LoggerFactory.getLogger(OrderAnalyticsKafkaListener.class);

    private final OrderAnalyticsAggregator aggregator;

    public OrderAnalyticsKafkaListener(OrderAnalyticsAggregator aggregator) {
        this.aggregator = aggregator;
    }

    @KafkaListener(
            topics = "${faster360.kafka.topics.analytics}",
            groupId = "${faster360.kafka.groups.analytics}",
            containerFactory = "orderAnalyticsListenerFactory")
    public void onAnalyticsEvent(ConsumerRecord<String, OrderAnalyticsEvent> record) {
        log.debug("[KAFKA ANALYTICS] Recibido {}-{}@{}", record.topic(), record.partition(), record.offset());
        aggregator.record(record.value());
    }
}
