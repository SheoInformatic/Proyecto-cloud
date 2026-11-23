package com.faster360.kafkaadminservice.config;

import org.apache.kafka.clients.admin.Admin;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaAdmin;

@Configuration
public class KafkaAdminClientConfig {

    /** Cliente Admin de Kafka reutilizando las propiedades (bootstrap servers) de spring.kafka.*. */
    @Bean(destroyMethod = "close")
    @org.springframework.context.annotation.Lazy
    public Admin kafkaAdminClient(KafkaAdmin kafkaAdmin) {
        return Admin.create(kafkaAdmin.getConfigurationProperties());
    }
}
