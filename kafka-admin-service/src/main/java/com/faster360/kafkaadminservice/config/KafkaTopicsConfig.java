package com.faster360.kafkaadminservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Declara los tópicos del sistema como beans NewTopic: KafkaAdmin los crea al arrancar
 * si no existen. Es la fuente de verdad de la infraestructura de streaming (replicable).
 */
@Configuration
public class KafkaTopicsConfig {

    @Bean
    public NewTopic ordersEventsTopic(TopicsProperties props) {
        return build(props.events());
    }

    @Bean
    public NewTopic ordersAnalyticsTopic(TopicsProperties props) {
        return build(props.analytics());
    }

    @Bean
    public NewTopic ordersEventsDltTopic(TopicsProperties props) {
        return build(props.dlt());
    }

    private NewTopic build(TopicsProperties.Topic t) {
        return TopicBuilder.name(t.name())
                .partitions(t.partitions())
                .replicas(t.replicas())
                .config(TopicConfig.RETENTION_MS_CONFIG, String.valueOf(t.retentionMs()))
                .config(TopicConfig.CLEANUP_POLICY_CONFIG, t.cleanupPolicy())
                .config(TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG, String.valueOf(t.minInsyncReplicas()))
                .build();
    }
}
