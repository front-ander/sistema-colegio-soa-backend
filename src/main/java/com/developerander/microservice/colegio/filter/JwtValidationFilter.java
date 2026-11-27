package com.developerander.microservice.colegio.filter;

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

// ══════════════════════════════════════════════════════════════════════
// FILTRO SOA TRANSVERSAL: JwtValidationFilter
//
// Reutilizable en TODOS los microservicios (8080 a 8084).
// Valida el token JWT (Authorization: Bearer <token>) emitido por auth-service (8085)
// usando la misma clave secreta HMAC-SHA256.
// Si falta o es inválido, responde 401 Unauthorized en JSON estándar.
// ══════════════════════════════════════════════════════════════════════
@Component
@Order(2)
public class JwtValidationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtValidationFilter.class);

    @Value("${app.security.jwt.enabled:false}")
    private boolean jwtEnabled;

    // Clave secreta compartida HMAC-SHA256 (mínimo 32 bytes / 256 bits)
    @Value("${app.security.jwt.secret:colegio-secret-key-apf3-2026-muy-larga-256bits}")
    private String jwtSecret;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // 1. Permitir peticiones preflight CORS (OPTIONS) sin token
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Rutas públicas excluidas de validación JWT (Swagger y Frontend estático)
        String path = request.getRequestURI();
        if (path.equals("/") ||
            path.equals("/index.html") ||
            path.equals("/favicon.ico") ||
            path.contains("/swagger-ui") ||
            path.contains("/v3/api-docs") ||
            path.contains("/swagger-resources") ||
            path.contains("/actuator")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Modo desarrollo si jwt.enabled=false
        if (!jwtEnabled) {
            log.debug("🔓 [JWT] Validación deshabilitada (modo desarrollo). Acceso permitido a {}", path);
            filterChain.doFilter(request, response);
            return;
        }

        // 4. Extraer cabecera Authorization: Bearer <token>
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("🚫 [ESB-JWT] Token ausente o formato inválido en: {}", path);
            enviarError401(response, "Token JWT requerido. Formato: Bearer <token>");
            return;
        }

        String token = authHeader.substring(7);

        try {
            // 5. Validar firma HMAC-SHA256 y expiración del token
            Claims claims = Jwts.parser()
                    .verifyWith(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)))
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String usuario = claims.getSubject();
            String rol = claims.get("rol", String.class);

            log.info("✅ [ESB-JWT] Acceso autorizado | Usuario: {} | Rol: {} | Ruta: {}", usuario, rol, path);

            // Propagar identidad y rol como atributos del request
            request.setAttribute("jwt-usuario", usuario);
            request.setAttribute("jwt-rol", rol);

            filterChain.doFilter(request, response);

        } catch (JwtException e) {
            log.warn("🚫 [ESB-JWT] Token inválido o expirado: {}", e.getMessage());
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
