package com.faster360.kafkaadminservice.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record UpdatePartitionsRequest(
        @Min(value = 1, message = "Debe tener al menos 1 partición")
        @Max(value = 100, message = "Máximo 100 particiones")
        int totalPartitions) {
}
