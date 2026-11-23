package com.faster360.notificationticketservice.config;

import com.faster360.notificationticketservice.dto.OrderAnalyticsEvent;
import com.faster360.notificationticketservice.dto.OrderCreatedEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuración del cliente consumidor Kafka:
 *  - brokers, deserializers tipados (JSON -> DTO) protegidos con ErrorHandlingDeserializer
 *  - consumer groups (definidos en cada @KafkaListener) y concurrencia por partición
 *  - manejo de errores: reintentos con backoff exponencial y Dead Letter Topic.
 */
@Configuration
public class KafkaConsumerConfig {

    private static final Logger log = LoggerFactory.getLogger(KafkaConsumerConfig.class);

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;
    @Value("${faster360.kafka.topics.dlt}")
    private String dltTopic;
    @Value("${faster360.kafka.consumer.concurrency}")
    private int concurrency;
    @Value("${faster360.kafka.consumer.auto-startup:true}")
    private boolean autoStartup;
    @Value("${faster360.kafka.consumer.max-poll-records}")
    private int maxPollRecords;
    @Value("${faster360.kafka.consumer.session-timeout-ms}")
    private int sessionTimeoutMs;
    @Value("${faster360.kafka.retry.max-attempts}")
    private int retryAttempts;
    @Value("${faster360.kafka.retry.initial-interval-ms}")
    private long retryInitialInterval;
    @Value("${faster360.kafka.retry.multiplier}")
    private double retryMultiplier;
    @Value("${faster360.kafka.retry.max-interval-ms}")
    private long retryMaxInterval;

    // ------------------------------------------------------------ CONSUMIDORES

    private Map<String, Object> baseConsumerProps() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);          // commit manejado por el contenedor
        props.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, maxPollRecords);
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, sessionTimeoutMs);
        props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, sessionTimeoutMs / 3);
        props.put(ConsumerConfig.PARTITION_ASSIGNMENT_STRATEGY_CONFIG,
                "org.apache.kafka.clients.consumer.CooperativeStickyAssignor");
        return props;
    }

    private <T> ConsumerFactory<String, T> typedFactory(Class<T> type) {
        return new DefaultKafkaConsumerFactory<>(
                baseConsumerProps(),
                new StringDeserializer(),
                new ErrorHandlingDeserializer<>(new JsonKafkaDeserializer<>(type)));
    }

    private <T> ConcurrentKafkaListenerContainerFactory<String, T> listenerFactory(
            ConsumerFactory<String, T> consumerFactory, DefaultErrorHandler errorHandler) {
        ConcurrentKafkaListenerContainerFactory<String, T> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setConcurrency(concurrency);
        factory.setAutoStartup(autoStartup);
        factory.setCommonErrorHandler(errorHandler);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderCreatedEvent> orderEventsListenerFactory(
            DefaultErrorHandler kafkaErrorHandler) {
        return listenerFactory(typedFactory(OrderCreatedEvent.class), kafkaErrorHandler);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderAnalyticsEvent> orderAnalyticsListenerFactory(
            DefaultErrorHandler kafkaErrorHandler) {
        return listenerFactory(typedFactory(OrderAnalyticsEvent.class), kafkaErrorHandler);
    }

    /** El listener del DLT lee los bytes originales sin deserializar, para poder auditar cualquier mensaje. */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, byte[]> deadLetterListenerFactory() {
        ConsumerFactory<String, byte[]> cf = new DefaultKafkaConsumerFactory<>(
                baseConsumerProps(), new StringDeserializer(), new ByteArrayDeserializer());
        ConcurrentKafkaListenerContainerFactory<String, byte[]> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(cf);
        factory.setConcurrency(1);
        factory.setAutoStartup(autoStartup);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);
        return factory;
    }

    // ------------------------------------------------------- MANEJO DE ERRORES

    /** Plantilla para republicar en el DLT: JSON para objetos ya deserializados. */
    @Bean
    public KafkaTemplate<String, Object> dltJsonTemplate() {
        return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(dltProducerProps(JsonKafkaSerializer.class)));
    }

    /** Plantilla para republicar en el DLT: bytes originales de mensajes que no pudieron deserializarse. */
    @Bean
    public KafkaTemplate<String, byte[]> dltBytesTemplate() {
        return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(dltProducerProps(ByteArraySerializer.class)));
    }

    private Map<String, Object> dltProducerProps(Class<?> valueSerializer) {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, valueSerializer);
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        props.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 5_000);
        return props;
    }

    /**
     * Reintentos con backoff exponencial; al agotarlos el registro se publica en el DLT
     * (con las cabeceras de la excepción) y el consumidor continúa con el siguiente mensaje.
     * Los errores de contenido (payload inválido) no se reintentan: van directo al DLT.
     */
    @Bean
    public DefaultErrorHandler kafkaErrorHandler(KafkaTemplate<String, Object> dltJsonTemplate,
                                                 KafkaTemplate<String, byte[]> dltBytesTemplate) {
        Map<Class<?>, KafkaOperations<?, ?>> templates = new HashMap<>();
        templates.put(byte[].class, dltBytesTemplate);
        templates.put(Object.class, dltJsonTemplate);

        // partición -1 => el productor elige (el DLT puede tener distinto número de particiones)
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                templates, (record, ex) -> new TopicPartition(dltTopic, -1));

        ExponentialBackOffWithMaxRetries backOff = new ExponentialBackOffWithMaxRetries(retryAttempts);
        backOff.setInitialInterval(retryInitialInterval);
        backOff.setMultiplier(retryMultiplier);
        backOff.setMaxInterval(retryMaxInterval);

        DefaultErrorHandler handler = new DefaultErrorHandler(recoverer, backOff);
        handler.addNotRetryableExceptions(
                com.faster360.notificationticketservice.service.InvalidEventException.class,
                org.apache.kafka.common.errors.SerializationException.class);
        handler.setRetryListeners((record, ex, attempt) ->
                log.warn("[KAFKA RETRY] Intento {} fallido para {}-{}@{}: {}",
                        attempt, record.topic(), record.partition(), record.offset(), ex.getMessage()));
        return handler;
    }
}
