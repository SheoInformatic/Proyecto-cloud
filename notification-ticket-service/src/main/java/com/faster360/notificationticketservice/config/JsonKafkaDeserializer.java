package com.faster360.notificationticketservice.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;

/**
 * Deserializer JSON hacia un DTO concreto. Tolera campos desconocidos (evolución del contrato).
 * Se envuelve en ErrorHandlingDeserializer: un mensaje malformado ("poison pill") no detiene el
 * consumidor, sino que se deriva al manejador de errores y al Dead Letter Topic.
 */
public class JsonKafkaDeserializer<T> implements Deserializer<T> {

    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private final Class<T> targetType;

    public JsonKafkaDeserializer(Class<T> targetType) {
        this.targetType = targetType;
    }

    @Override
    public T deserialize(String topic, byte[] data) {
        if (data == null) {
            return null;
        }
        try {
            return mapper.readValue(data, targetType);
        } catch (Exception e) {
            throw new SerializationException("JSON inválido en el tópico " + topic
                    + " para " + targetType.getSimpleName() + ": " + e.getMessage(), e);
        }
    }
}
