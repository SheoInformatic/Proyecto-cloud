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
                if (!iss.contains("faster360.com")) {
                    log.warn("Emisor del token no confiable: {}", iss);
                    return false;
                }
            }

            // 3. Extraer usuario (claim 'sub')
            Matcher subMatcher = SUB_PATTERN.matcher(payloadJson);
            String sub = subMatcher.find() ? subMatcher.group(1) : "desconocido";

            log.info("JWT validado con éxito para el usuario: {}", sub);
            return true;

        } catch (Exception e) {
            log.error("Error al validar el token JWT: {}", e.getMessage());
            return false;
        }
    }
}