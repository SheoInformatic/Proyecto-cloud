package com.faster360.kafkaadminservice.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/** Exige un JWT de administrador del IDaaS en todas las peticiones (401 sin token válido, 403 sin rol). */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtAdminValidator validator;

    public JwtAuthenticationFilter(JwtAdminValidator validator) {
        this.validator = validator;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        // Se autentica TODO salvo el pre-flight CORS (que no lleva credenciales). No se decide
        // según un prefijo de URL para evitar bypass por rutas con ';', '//' o codificaciones.
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            chain.doFilter(request, response);
            return;
        }

        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            reject(response, HttpStatus.UNAUTHORIZED, "Acceso no autorizado: token JWT ausente");
            return;
        }

        JwtAdminValidator.Outcome outcome = validator.validate(header.substring(7));
        switch (outcome.result()) {
            case OK -> {
                log.info("[ADMIN] {} {} por {}", request.getMethod(), request.getRequestURI(), outcome.email());
                chain.doFilter(request, response);
            }
            case FORBIDDEN -> {
                log.warn("[ADMIN] Acceso denegado a {}: {}", outcome.email(), outcome.reason());
                reject(response, HttpStatus.FORBIDDEN, "Acceso denegado: " + outcome.reason());
            }
            default -> {
                log.warn("[ADMIN] Token rechazado: {}", outcome.reason());
                reject(response, HttpStatus.UNAUTHORIZED, "Acceso no autorizado: " + outcome.reason());
            }
        }
    }

    private void reject(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"status\":" + status.value() + ",\"error\":\"" + message + "\"}");
    }
}
