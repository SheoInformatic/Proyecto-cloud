package com.faster360.kafkaadminservice.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.util.Map;

public record CreateTopicRequest(
        @NotBlank(message = "El nombre del tópico es obligatorio")
        @Pattern(regexp = "[a-zA-Z0-9._-]+", message = "Nombre inválido: solo letras, números, '.', '_' y '-'")
        String name,

        @Min(value = 1, message = "Debe tener al menos 1 partición")
        @Max(value = 100, message = "Máximo 100 particiones")
        int partitions,

        @Min(value = 1, message = "El factor de replicación mínimo es 1")
        @Max(value = 3, message = "El factor de replicación máximo es 3 (brokers disponibles)")
        short replicationFactor,

        /** Configuraciones opcionales: retention.ms, cleanup.policy, min.insync.replicas, etc. */
        Map<String, String> configs) {
}
