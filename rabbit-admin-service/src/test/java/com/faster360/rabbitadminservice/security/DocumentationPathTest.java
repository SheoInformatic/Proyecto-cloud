package com.faster360.rabbitadminservice.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** La documentación se sirve sin token, pero solo en rutas exactas y sin trucos de URL. */
class DocumentationPathTest {

    @Test
    void permiteSoloLasRutasDeDocumentacion() {
        assertTrue(JwtAuthenticationFilter.isDocumentationPath("/v3/api-docs"));
        assertTrue(JwtAuthenticationFilter.isDocumentationPath("/v3/api-docs/swagger-config"));
        assertTrue(JwtAuthenticationFilter.isDocumentationPath("/swagger-ui.html"));
        assertTrue(JwtAuthenticationFilter.isDocumentationPath("/swagger-ui/index.html"));
    }

    @Test
    void lasRutasDeLaApiSiguenProtegidas() {
        assertFalse(JwtAuthenticationFilter.isDocumentationPath("/api/admin/queues"));
        assertFalse(JwtAuthenticationFilter.isDocumentationPath("/api/kafka/topics"));
        assertFalse(JwtAuthenticationFilter.isDocumentationPath("/"));
        assertFalse(JwtAuthenticationFilter.isDocumentationPath(null));
    }

    @Test
    void losTruquitosDeUrlNoSaltanLaProteccion() {
        assertFalse(JwtAuthenticationFilter.isDocumentationPath("/swagger-ui/../api/kafka/topics"));
        assertFalse(JwtAuthenticationFilter.isDocumentationPath("/swagger-ui/;/api/kafka/topics"));
        assertFalse(JwtAuthenticationFilter.isDocumentationPath("/swagger-ui/%2e%2e/api/kafka/topics"));
        assertFalse(JwtAuthenticationFilter.isDocumentationPath("/swagger-ui//api/kafka/topics"));
        assertFalse(JwtAuthenticationFilter.isDocumentationPath("/v3/api-docs;x=1"));
    }
}
