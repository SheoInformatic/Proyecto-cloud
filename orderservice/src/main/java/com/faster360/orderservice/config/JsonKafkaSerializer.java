package com.faster360.orderservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;

/**
 * Serializer Kafka que convierte DTOs a JSON (UTF-8) con Jackson.
 * Soporta fechas java.time en formato ISO-8601 y BigDecimal sin pérdida de precisión.
 */
public class JsonKafkaSerializer<T> implements Serializer<T> {

    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Override
    public byte[] serialize(String topic, T data) {
        if (data == null) {
            return null;
        }
        try {
            return mapper.writeValueAsBytes(data);
        } catch (Exception e) {
            throw new SerializationException("No se pudo serializar el mensaje para el tópico " + topic, e);
        }
    }
}
