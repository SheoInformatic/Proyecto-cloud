package com.faster360.rabbitadminservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class CreateExchangeRequest {

    @NotBlank(message = "El nombre del exchange no puede estar vacío")
    private String name;

    @NotBlank(message = "El tipo de exchange es obligatorio")
    @Pattern(regexp = "^(direct|topic|fanout|headers)$", flags = Pattern.Flag.CASE_INSENSITIVE,
            message = "El tipo debe ser: direct, topic, fanout o headers")
    private String type;

    private boolean durable = true;
    private boolean autoDelete = false;

    public CreateExchangeRequest() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public boolean isDurable() { return durable; }
    public void setDurable(boolean durable) { this.durable = durable; }

    public boolean isAutoDelete() { return autoDelete; }
    public void setAutoDelete(boolean autoDelete) { this.autoDelete = autoDelete; }
}