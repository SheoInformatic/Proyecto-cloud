package com.faster360.kafkaadminservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Parámetros de administración de los tópicos del sistema (particiones, réplicas,
 * retención y cleanup policy), enlazados desde faster360.kafka.topics.* del application.yaml.
 */
@ConfigurationProperties(prefix = "faster360.kafka.topics")
public record TopicsProperties(Topic events, Topic analytics, Topic dlt) {

    public record Topic(String name,
                        int partitions,
                        short replicas,
                        long retentionMs,
                        String cleanupPolicy,
                        int minInsyncReplicas) {
    }
}
