package com.developerander.microservice.colegio.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

// ══════════════════════════════════════════════════════════════════════
// FILTRO SOA: CorrelationIdFilter
//
// Simula lo que hace el ESB (WSO2 Micro Integrator) en producción:
// GENERAR un X-Correlation-ID único por cada petición HTTP.
//
// ¿Para qué sirve X-Correlation-ID?
//   - Es un UUID que identifica TODA la cadena de una petición SOA
//   - Si el cliente hace POST /api/v1/matriculas, ese ID viaja por:
//     Controller → Service → ProcesoMatricula → TesoreriaClient → AulaClient → RabbitMQ
//   - Cuando algo falla, buscas ese ID en los logs de TODOS los servicios
//     y reconstruyes exactamente qué pasó y en qué paso falló
//   - Es el estándar de trazabilidad distribuida en SOA/Microservicios
//
// @Component = Spring lo registra como bean y lo ejecuta automáticamente
// @Order(1)  = Se ejecuta PRIMERO antes que cualquier otro filtro
// OncePerRequestFilter = garantiza que se ejecute UNA SOLA VEZ por petición
// ══════════════════════════════════════════════════════════════════════
@Component
@Order(1) // Prioridad máxima: este filtro va primero que el JWT
public class CorrelationIdFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(CorrelationIdFilter.class);

    // Nombre del header HTTP estándar para el ID de correlación
    public static final String CORRELATION_ID_HEADER = "X-Correlation-ID";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // 1. Ver si el cliente ya envió un X-Correlation-ID (ej: el ESB lo generó upstream)
        String correlationId = request.getHeader(CORRELATION_ID_HEADER);

        // 2. Si no existe, generamos uno nuevo (UUID aleatorio garantiza unicidad global)
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
            log.info("🔑 [ESB-SIM] X-Correlation-ID generado: {}", correlationId);
        } else {
            log.info("🔑 [ESB-SIM] X-Correlation-ID recibido del cliente: {}", correlationId);
        }

        // 3. Guardamos el correlationId como atributo de la request
        //    para que ProcesoMatricula pueda leerlo y adjuntarlo al evento RabbitMQ
        request.setAttribute(CORRELATION_ID_HEADER, correlationId);

        // 4. Lo incluimos en la RESPUESTA también, para que el cliente sepa su ID
        response.setHeader(CORRELATION_ID_HEADER, correlationId);

        // 5. Pasamos la petición al siguiente filtro (JwtValidationFilter → Controller)
        filterChain.doFilter(request, response);
    }
}
