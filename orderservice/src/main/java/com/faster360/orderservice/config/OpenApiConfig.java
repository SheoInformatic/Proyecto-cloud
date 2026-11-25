package com.faster360.orderservice.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Documentación OpenAPI (Swagger UI en /swagger-ui.html, especificación en /v3/api-docs). */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI faster360OpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("OrderService")
                        .version("1.0.0")
                        .description("Recibe pedidos, los guarda en PostgreSQL y publica eventos en RabbitMQ y Kafka."))
                .components(new Components().addSecuritySchemes("bearerAuth",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}
