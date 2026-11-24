package com.faster360.kafkaadminservice.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.Map;

public record UpdateTopicConfigRequest(
        @NotEmpty(message = "Debe indicar al menos una configuración (ej. retention.ms)")
        Map<String, String> configs) {
}
