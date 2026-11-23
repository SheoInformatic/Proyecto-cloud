package com.faster360.orderservice.config;

import com.faster360.orderservice.dto.OrderAnalyticsEvent;
import com.faster360.orderservice.dto.OrderCreatedEvent;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuración del cliente productor Kafka: brokers, serializers y garantías de entrega.
 * Un KafkaTemplate tipado por DTO para cada tópico.
 */
@Configuration
@EnableConfigurationProperties(KafkaTopicsProperties.class)
public class KafkaProducerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    private Map<String, Object> baseProducerProps() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonKafkaSerializer.class);
        // Garantías de entrega: sin pérdidas ni duplicados ante reintentos
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        props.put(ProducerConfig.RETRIES_CONFIG, 5);
        props.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, 500);
        props.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, 30_000);
        props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 10_000);
        // No bloquear el hilo HTTP más de 5s si el clúster no está disponible
        props.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 5_000);
        // Rendimiento: agrupar y comprimir lotes
        props.put(ProducerConfig.LINGER_MS_CONFIG, 10);
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 32 * 1024);
        props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "lz4");
        return props;
    }

    @Bean
    public ProducerFactory<String, OrderCreatedEvent> orderEventProducerFactory() {
        return new DefaultKafkaProducerFactory<>(baseProducerProps());
    }

    @Bean
    public KafkaTemplate<String, OrderCreatedEvent> orderEventKafkaTemplate(
            ProducerFactory<String, OrderCreatedEvent> orderEventProducerFactory) {
        return new KafkaTemplate<>(orderEventProducerFactory);
    }

    @Bean
    public ProducerFactory<String, OrderAnalyticsEvent> orderAnalyticsProducerFactory() {
        return new DefaultKafkaProducerFactory<>(baseProducerProps());
    }

    @Bean
    public KafkaTemplate<String, OrderAnalyticsEvent> orderAnalyticsKafkaTemplate(
            ProducerFactory<String, OrderAnalyticsEvent> orderAnalyticsProducerFactory) {
        return new KafkaTemplate<>(orderAnalyticsProducerFactory);
    }
}
