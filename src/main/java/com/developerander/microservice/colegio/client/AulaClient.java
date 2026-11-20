package com.developerander.microservice.colegio.client;

import com.developerander.microservice.colegio.dto.VacanteResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

// ══════════════════════════════════════════════════════════════════════
// CLIENTE HTTP: AulaClient
//
// Llama a AulaService para verificar si el aula tiene vacantes.
// Mismo patrón que TesoreriaClient: Anti-Corruption Layer.
//
// Si AulaService no está disponible → stub con 5 vacantes disponibles
// (modo prototipo APF3, en producción: Circuit Breaker)
// ══════════════════════════════════════════════════════════════════════
@Component
public class AulaClient {

    private static final Logger log = LoggerFactory.getLogger(AulaClient.class);

    @Autowired
    private RestTemplate restTemplate;

    // URL base de AulaService (ej: http://localhost:8082)
    @Value("${servicios.aula.url}")
    private String aulaUrl;

    // ── CONSULTAR VACANTES DE UN AULA ─────────────────────────────────
    public VacanteResponse consultarVacantes(UUID idAula) {
        // URL: http://localhost:8082/api/v1/aulas/{id}/vacantes
        String url = aulaUrl + "/api/v1/aulas/" + idAula + "/vacantes";

        try {
            log.info("🔗 [SOA] Llamando a AulaService: GET {}", url);

            VacanteResponse respuesta = restTemplate.getForObject(url, VacanteResponse.class);

            log.info("✅ [SOA] AulaService respondió: vacantesDisponibles={}",
                    respuesta != null ? respuesta.getVacantesDisponibles() : "null");

            return respuesta;

        } catch (RestClientException e) {
            // AulaService no disponible → STUB con vacantes disponibles
            log.warn("⚠️ [SOA] AulaService no disponible en {}. Usando respuesta STUB (5 vacantes).", url);

            // Stub: simula que el aula tiene 5 vacantes disponibles
            return new VacanteResponse(
                    idAula.toString(),
                    "STUB-AULA",
                    30,     // capacidad máxima
                    25,     // matriculados
                    5       // vacantesDisponibles
            );
        }
    }
}
