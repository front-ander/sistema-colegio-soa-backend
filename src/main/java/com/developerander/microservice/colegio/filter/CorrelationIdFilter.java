package com.developerander.microservice.colegio.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

// ══════════════════════════════════════════════════════════════════════
// FILTRO SOA TRANSVERSAL: CorrelationIdFilter
//
// Reutilizable en TODOS los microservicios (8080 a 8084).
// 1. Lee la cabecera X-Correlation-ID o genera un UUID nuevo si no existe.
// 2. La guarda en el MDC de SLF4J para que aparezca en TODOS los logs.
// 3. La devuelve en el Response Header y la limpia en el bloque finally.
// ══════════════════════════════════════════════════════════════════════
@Component
@Order(1)
public class CorrelationIdFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(CorrelationIdFilter.class);

    public static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    public static final String MDC_CORRELATION_KEY = "correlationId";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // 1. Leer cabecera entrante o generar nuevo UUID
        String correlationId = request.getHeader(CORRELATION_ID_HEADER);
        boolean generado = false;

        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
            generado = true;
        }

        // 2. Guardar en MDC de SLF4J (aparecerá automáticamente en cada log del hilo)
        MDC.put(MDC_CORRELATION_KEY, correlationId);

        // 3. Guardar en atributo del request y agregar en cabecera de respuesta
        request.setAttribute(CORRELATION_ID_HEADER, correlationId);
        response.setHeader(CORRELATION_ID_HEADER, correlationId);

        try {
            if (generado) {
                log.info("🔑 [ESB-CORR] X-Correlation-ID generado: {} | {} {}",
                        correlationId, request.getMethod(), request.getRequestURI());
            } else {
                log.info("🔑 [ESB-CORR] X-Correlation-ID propagado: {} | {} {}",
                        correlationId, request.getMethod(), request.getRequestURI());
            }

            // 4. Continuar con la cadena de filtros (JwtValidationFilter -> Controller)
            filterChain.doFilter(request, response);
        } finally {
            // 5. OBLIGATORIO: Limpiar el MDC al terminar la petición para evitar fugas entre hilos
            MDC.remove(MDC_CORRELATION_KEY);
        }
    }
}
