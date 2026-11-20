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
// FILTRO SOA: JwtValidationFilter
//
// Simula la validación de tokens JWT que hace el ESB (WSO2) en producción.
//
// Flujo de autenticación SOA:
//   1. Cliente obtiene JWT de un AuthService (login)
//   2. Cliente envía: Authorization: Bearer <token> en cada petición
//   3. Este filtro (o WSO2 en producción) valida el token:
//      ✅ Token válido → pasa al controller
//      ❌ Token inválido/vencido → devuelve 401 Unauthorized
//
// CONFIG: jwt.enabled=false en application.properties para deshabilitar
// en desarrollo y poder usar Swagger UI sin token.
//
// @Order(2) = Se ejecuta DESPUÉS de CorrelationIdFilter
// ══════════════════════════════════════════════════════════════════════
@Component
@Order(2)
public class JwtValidationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtValidationFilter.class);

    // Si false: el filtro deja pasar todas las peticiones sin validar JWT
    // Cambiar a true en producción o para simular el ESB completo
    @Value("${app.security.jwt.enabled:false}")
    private boolean jwtEnabled;

    // Clave secreta para firmar/verificar el JWT (mínimo 256 bits = 32 caracteres)
    @Value("${app.security.jwt.secret:colegio-secret-key-apf3-2026-muy-larga}")
    private String jwtSecret;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // ── Rutas excluidas de validación JWT ─────────────────────────
        // Swagger UI y el spec JSON siempre pasan sin token (son públicos)
        String path = request.getRequestURI();
        if (path.contains("/swagger-ui") ||
            path.contains("/v3/api-docs") ||
            path.contains("/swagger-resources") ||
            path.contains("/actuator")) {
            filterChain.doFilter(request, response); // Pasa sin validar
            return;
        }

        // ── Si JWT está deshabilitado, pasa directo ───────────────────
        if (!jwtEnabled) {
            log.debug("🔓 [JWT] Validación deshabilitada (modo desarrollo). Petición permitida.");
            filterChain.doFilter(request, response);
            return;
        }

        // ── Validación JWT activa ─────────────────────────────────────
        // Extrae el header "Authorization: Bearer <token>"
        String authHeader = request.getHeader("Authorization");

        // Si no hay header o no empieza con "Bearer " → 401
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("🚫 [ESB-JWT] Token ausente o malformado en petición a: {}", path);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);           // 401
            response.setContentType("application/json");
            response.getWriter().write("""
                    {"status":401,"error":"No autorizado","mensaje":"Token JWT requerido. Formato: Bearer <token>"}
                    """);
            return; // Corta el filtro aquí, no llega al controller
        }

        // Extrae solo el token (quita el prefijo "Bearer ")
        String token = authHeader.substring(7);

        try {
            // Verifica la firma del token con la clave secreta
            // Si el token fue alterado o expiró, lanza JwtException
            Claims claims = Jwts.parser()
                    .verifyWith(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)))
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            // Extrae información del usuario del JWT para logging
            String usuario = claims.getSubject(); // "sub" claim = nombre del usuario
            log.info("✅ [ESB-JWT] Token válido para usuario: {} → acceso permitido a {}", usuario, path);

            // Pasa la identidad del usuario a la request para usarla en el controller si se necesita
            request.setAttribute("jwt-usuario", usuario);

            filterChain.doFilter(request, response); // Token válido → pasa al controller

        } catch (JwtException e) {
            // Token inválido (firma incorrecta, expirado, mal formado)
            log.warn("🚫 [ESB-JWT] Token inválido: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);           // 401
            response.setContentType("application/json");
            response.getWriter().write("""
                    {"status":401,"error":"Token inválido","mensaje":"El token JWT es inválido o ha expirado"}
                    """);
        }
    }
}
