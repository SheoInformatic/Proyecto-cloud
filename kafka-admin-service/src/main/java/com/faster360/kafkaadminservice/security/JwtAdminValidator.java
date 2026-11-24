package com.faster360.kafkaadminservice.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Arrays;
import java.util.Base64;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Valida el JWT emitido por el IDaaS (GCP Identity Platform / Google OAuth) para la API de
 * administración. Es más estricto que el validador de orderservice: exige exp, iss y email,
 * y solo autoriza a las cuentas listadas como administradoras.
 *
 * Limitación conocida (igual que orderservice): no verifica la firma criptográfica contra las
 * claves públicas (JWKS) de Google; valida estructura y claims.
 */
@Component
public class JwtAdminValidator {

    public enum Result { OK, UNAUTHENTICATED, FORBIDDEN }

    public record Outcome(Result result, String email, String reason) {
        static Outcome ok(String email) { return new Outcome(Result.OK, email, null); }
        static Outcome unauthenticated(String reason) { return new Outcome(Result.UNAUTHENTICATED, null, reason); }
        static Outcome forbidden(String email, String reason) { return new Outcome(Result.FORBIDDEN, email, reason); }
    }

    private static final Logger log = LoggerFactory.getLogger(JwtAdminValidator.class);
    private static final JsonMapper MAPPER = JsonMapper.builder().build();

    private final Set<String> adminEmails;

    public JwtAdminValidator(@Value("${faster360.security.admin-emails}") String adminEmails) {
        this.adminEmails = Arrays.stream(adminEmails.split(","))
                .map(s -> s.trim().toLowerCase())
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
    }

    public Outcome validate(String token) {
        if (token == null || token.isBlank()) {
            return Outcome.unauthenticated("Token ausente");
        }
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3 || parts[2].isEmpty()) {
                return Outcome.unauthenticated("Estructura JWT inválida");
            }

            JsonNode header = decode(parts[0]);
            String alg = header.path("alg").asString("");
            if (alg.isBlank() || alg.equalsIgnoreCase("none")) {
                return Outcome.unauthenticated("Algoritmo de firma no aceptado");
            }

            JsonNode payload = decode(parts[1]);

            if (!payload.hasNonNull("exp") || Instant.now().getEpochSecond() > payload.get("exp").asLong()) {
                return Outcome.unauthenticated("Token expirado o sin 'exp'");
            }

            String iss = payload.path("iss").asString("");
            if (!(iss.contains("securetoken.google.com") || iss.contains("accounts.google.com"))) {
                return Outcome.unauthenticated("Emisor no confiable");
            }

            String email = payload.path("email").asString("").trim().toLowerCase();
            if (email.isEmpty() || !email.contains("@")) {
                return Outcome.unauthenticated("Token sin 'email'");
            }
            if (payload.has("email_verified") && !payload.get("email_verified").asBoolean(false)) {
                return Outcome.unauthenticated("Correo no verificado");
            }

            if (!adminEmails.contains(email)) {
                return Outcome.forbidden(email, "La cuenta no tiene rol de administrador");
            }
            return Outcome.ok(email);
        } catch (Exception e) {
            log.warn("JWT no procesable: {}", e.getMessage());
            return Outcome.unauthenticated("Token no procesable");
        }
    }

    private JsonNode decode(String segment) {
        byte[] bytes = Base64.getUrlDecoder().decode(segment);
        return MAPPER.readTree(new String(bytes, StandardCharsets.UTF_8));
    }
}
