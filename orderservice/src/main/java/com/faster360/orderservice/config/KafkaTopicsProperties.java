package com.faster360.orderservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Nombres de los tópicos Kafka del sistema (faster360.kafka.topics.*). */
@ConfigurationProperties(prefix = "faster360.kafka.topics")
public record KafkaTopicsProperties(String events, String analytics) {
}
