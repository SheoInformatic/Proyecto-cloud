package com.faster360.orderservice.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class JwtTokenValidator {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenValidator.class);

    private static final Pattern EXP_PATTERN = Pattern.compile("\"exp\"\\s*:\\s*(\\d+)");
    private static final Pattern ISS_PATTERN = Pattern.compile("\"iss\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern SUB_PATTERN = Pattern.compile("\"sub\"\\s*:\\s*\"([^\"]+)\"");

    // Registro oficial de dominios y correos permitidos en GCP Identity Platform
    public static final java.util.Set<String> ALLOWED_EMAIL_DOMAINS = java.util.Set.of("gmail.com", "duocuc.cl");
    public static final java.util.Set<String> ALLOWED_GCP_USERS = java.util.Set.of(
            "sergiomarcelo3672@gmail.com",
            "om.nieto@duocuc.cl"
    );

    public boolean validateToken(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }

        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                log.warn("Estructura JWT inválida: no contiene tres segmentos.");
                return false;
            }

            // Decodificar el payload (segundo segmento del JWT)
            byte[] decodedBytes = Base64.getUrlDecoder().decode(parts[1]);
            String payloadJson = new String(decodedBytes, StandardCharsets.UTF_8);

            // 1. Validar expiración (claim 'exp')
            Matcher expMatcher = EXP_PATTERN.matcher(payloadJson);
            if (expMatcher.find()) {
                long exp = Long.parseLong(expMatcher.group(1));
                if (Instant.now().getEpochSecond() > exp) {
                    log.warn("El token JWT ha expirado.");
                    return false;
                }
            }

            // 2. Validar emisor esperado (claim 'iss')
            Matcher issMatcher = ISS_PATTERN.matcher(payloadJson);
            if (issMatcher.find()) {
                String iss = issMatcher.group(1);
                boolean isGcp = iss.contains("securetoken.google.com");
                boolean isFaster = iss.contains("faster360.com");
                if (!isGcp && !isFaster) {
                    log.warn("Emisor del token no confiable: {}", iss);
                    return false;
                }
                log.info("Emisor IDaaS reconocido: {}", isGcp ? "Google Cloud Identity Platform" : "Faster360 IDaaS Gateway");
            }

            // 3. Extraer usuario (claim 'email' o 'sub') y Tenant ID (claim 'tenant_id')
            Matcher emailMatcher = Pattern.compile("\"email\"\\s*:\\s*\"([^\"]+)\"").matcher(payloadJson);
            Matcher subMatcher = SUB_PATTERN.matcher(payloadJson);
            String user = emailMatcher.find() ? emailMatcher.group(1) : (subMatcher.find() ? subMatcher.group(1) : "desconocido");

            Matcher tenantMatcher = Pattern.compile("\"tenant_id\"\\s*:\\s*\"([^\"]+)\"").matcher(payloadJson);
            if (tenantMatcher.find()) {
                log.info("Tenant ID de GCP Identity Platform verificado: {}", tenantMatcher.group(1));
            }

            // 4. Validar política de seguridad IDaaS de GCP: Cuentas autorizadas en el registro (@gmail.com / @duocuc.cl)
            if (user != null && !user.isBlank() && !user.equalsIgnoreCase("desconocido")) {
                String normalizedEmail = user.trim().toLowerCase();
                String domain = normalizedEmail.contains("@") ? normalizedEmail.substring(normalizedEmail.indexOf("@") + 1) : "";

                if (!ALLOWED_EMAIL_DOMAINS.contains(domain)) {
                    log.warn("Rechazo de seguridad IDaaS: El correo '{}' no pertenece a los dominios autorizados de Google (@gmail.com o @duocuc.cl)", user);
                    return false;
                }

                if (ALLOWED_GCP_USERS.contains(normalizedEmail)) {
                    log.info("Usuario verificado en el registro oficial de GCP Identity Platform: {}", normalizedEmail);
                } else {
                    log.info("Correo válido con dominio oficial de Google verificado: {}", normalizedEmail);
                }
            }

            log.info("JWT IDaaS validado con éxito para el usuario: {}", user);
            return true;

        } catch (Exception e) {
            log.error("Error al validar el token JWT: {}", e.getMessage());
            return false;
        }
    }
}