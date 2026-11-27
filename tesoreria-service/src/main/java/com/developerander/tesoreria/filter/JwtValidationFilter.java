package com.developerander.tesoreria.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

// Filtro transversal SOA: valida el token Bearer JWT propagado desde matricula-service
// usando la misma clave HMAC-SHA256 que auth-service (8085).
@Component
@Order(2)
public class JwtValidationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtValidationFilter.class);

    @Value("${app.security.jwt.enabled:true}")
    private boolean jwtEnabled;

    @Value("${app.security.jwt.secret:colegio-secret-key-apf3-2026-muy-larga-256bits}")
    private String jwtSecret;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String path = request.getRequestURI();
        if (path.contains("/swagger-ui") || path.contains("/v3/api-docs")) {
            filterChain.doFilter(request, response);
            return;
        }

        if (!jwtEnabled) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            enviarError401(response, "Token JWT requerido. Formato: Bearer <token>");
            return;
        }

        String token = authHeader.substring(7);
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)))
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            log.info("✅ [TESORERIA-JWT] Autorizado | Usuario: {} | Rol: {}",
                    claims.getSubject(), claims.get("rol", String.class));
            filterChain.doFilter(request, response);
        } catch (JwtException e) {
            log.warn("🚫 [TESORERIA-JWT] Token inválido: {}", e.getMessage());
            enviarError401(response, "El token JWT es inválido o ha expirado");
        }
    }

    private void enviarError401(HttpServletResponse response, String mensaje) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(String.format(
                "{\"status\":401,\"error\":\"Unauthorized\",\"mensaje\":\"%s\"}",
                mensaje.replace("\"", "\\\"")
        ));
    }
}
