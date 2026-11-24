package com.faster360.kafkaadminservice.security;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class JwtSecurityTest {

    private static final String ADMIN = "om.nieto@duocuc.cl";

    private final JwtAdminValidator validator = new JwtAdminValidator(ADMIN);
    private final JwtAuthenticationFilter filter = new JwtAuthenticationFilter(validator);

    private static String b64(String json) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(json.getBytes(StandardCharsets.UTF_8));
    }

    private static String token(String alg, String claims) {
        return b64("{\"alg\":\"" + alg + "\"}") + "." + b64(claims) + ".c2ln";
    }

    private static String claims(String email, long expOffset, String iss) {
        return "{\"iss\":\"" + iss + "\",\"email\":\"" + email + "\",\"exp\":"
                + (Instant.now().getEpochSecond() + expOffset) + "}";
    }

    private int call(String method, String uri, String authHeader, FilterChain chain) throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest(method, uri);
        if (authHeader != null) req.addHeader("Authorization", authHeader);
        MockHttpServletResponse res = new MockHttpServletResponse();
        filter.doFilter(req, res, chain);
        return res.getStatus();
    }

    @Test
    void adminWithValidTokenIsAllowed() throws Exception {
        FilterChain chain = mock(FilterChain.class);
        String t = token("RS256", claims(ADMIN, 3600, "https://securetoken.google.com/proj"));
        assertEquals(200, call("DELETE", "/api/kafka/topics/x", "Bearer " + t, chain));
        verify(chain).doFilter(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void missingTokenIs401() throws Exception {
        FilterChain chain = mock(FilterChain.class);
        assertEquals(401, call("GET", "/api/kafka/topics", null, chain));
        verify(chain, never()).doFilter(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void expiredTokenIs401() throws Exception {
        String t = token("RS256", claims(ADMIN, -10, "https://securetoken.google.com/proj"));
        assertEquals(401, call("GET", "/api/kafka/topics", "Bearer " + t, mock(FilterChain.class)));
    }

    @Test
    void tokenWithoutExpIs401() throws Exception {
        String t = token("RS256", "{\"iss\":\"https://securetoken.google.com/p\",\"email\":\"" + ADMIN + "\"}");
        assertEquals(401, call("GET", "/api/kafka/topics", "Bearer " + t, mock(FilterChain.class)));
    }

    @Test
    void algNoneIs401() throws Exception {
        String t = token("none", claims(ADMIN, 3600, "https://securetoken.google.com/proj"));
        assertEquals(401, call("GET", "/api/kafka/topics", "Bearer " + t, mock(FilterChain.class)));
    }

    @Test
    void untrustedIssuerIs401() throws Exception {
        String t = token("RS256", claims(ADMIN, 3600, "https://evil.example.com"));
        assertEquals(401, call("GET", "/api/kafka/topics", "Bearer " + t, mock(FilterChain.class)));
    }

    @Test
    void validNonAdminIs403() throws Exception {
        String t = token("RS256", claims("sergiomarcelo3672@gmail.com", 3600, "https://accounts.google.com"));
        assertEquals(403, call("DELETE", "/api/kafka/topics/x", "Bearer " + t, mock(FilterChain.class)));
    }

    @Test
    void unusualPathsAreAlsoProtected() throws Exception {
        FilterChain chain = mock(FilterChain.class);
        assertEquals(401, call("DELETE", "/api;x=1/kafka/topics/x", null, chain));
        assertEquals(401, call("DELETE", "//api/kafka/topics/x", null, chain));
        assertEquals(401, call("GET", "/otra-ruta", null, chain));
        verify(chain, never()).doFilter(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void preflightIsNotBlocked() throws Exception {
        FilterChain chain = mock(FilterChain.class);
        assertEquals(200, call("OPTIONS", "/api/kafka/topics", null, chain));
    }
}
