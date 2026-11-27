package com.developerander.tesoreria.filter;

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

// Filtro transversal SOA: lee X-Correlation-ID propagado desde matricula-service,
// lo guarda en MDC para trazabilidad en logs y lo devuelve en el response header.
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

        String correlationId = request.getHeader(CORRELATION_ID_HEADER);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }

        // Registra en SLF4J MDC para que aparezca en todos los logs de esta petición
        MDC.put(MDC_CORRELATION_KEY, correlationId);
        response.setHeader(CORRELATION_ID_HEADER, correlationId);

        try {
            log.info("🔑 [TESORERIA-CORR] {} {} | CorrelationId: {}",
                    request.getMethod(), request.getRequestURI(), correlationId);
            filterChain.doFilter(request, response);
        } finally {
            // Limpia el MDC al finalizar para no contaminar otras peticiones en el pool de hilos
            MDC.remove(MDC_CORRELATION_KEY);
        }
    }
}
