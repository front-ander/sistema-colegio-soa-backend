package com.developerander.microservice.colegio.client;

import com.developerander.microservice.colegio.dto.DeudaResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

// ══════════════════════════════════════════════════════════════════════
// CLIENTE HTTP: TesoreriaClient
//
// Responsabilidad: llamar a TesoreriaService (otro microservicio SOA)
// para verificar si un estudiante tiene deudas pendientes.
//
// En arquitectura SOA cada servicio es INDEPENDIENTE.
// No compartimos la BD; hacemos una llamada HTTP entre servicios.
//
// Patrón: Anti-Corruption Layer
//   Si TesoreriaService cambia su contrato, solo cambia este cliente.
//   El resto del código (ProcesoMatricula) no se afecta.
//
// PROTOTIPO APF3: Si TesoreriaService no está levantado, usa el
// modo stub (retorna "sin deuda") para no bloquear el desarrollo.
// ══════════════════════════════════════════════════════════════════════
@Component
public class TesoreriaClient {

    private static final Logger log = LoggerFactory.getLogger(TesoreriaClient.class);

    // RestTemplate = cliente HTTP síncrono de Spring para llamar otros servicios
    // Lo inyecta el bean definido en RestTemplateConfig
    @Autowired
    private RestTemplate restTemplate;

    // URL base de TesoreriaService leída desde application.properties
    // Permite cambiar la URL sin recompilar (útil para dev/staging/prod)
    @Value("${servicios.tesoreria.url}")
    private String tesoreriaUrl;

    // ── CONSULTAR DEUDAS DE UN ESTUDIANTE ─────────────────────────────
    public DeudaResponse consultarDeudas(UUID idEstudiante) {
        // Construye la URL completa: http://localhost:8081/api/v1/deudas/{id}
        String url = tesoreriaUrl + "/api/v1/deudas/" + idEstudiante;

        try {
            log.info("🔗 [SOA] Llamando a TesoreriaService: GET {}", url);

            // getForObject = hace GET HTTP y deserializa la respuesta JSON a DeudaResponse
            DeudaResponse respuesta = restTemplate.getForObject(url, DeudaResponse.class);

            log.info("✅ [SOA] TesoreriaService respondió: tieneDeuda={}, monto={}",
                    respuesta != null ? respuesta.isTieneDeuda() : "null",
                    respuesta != null ? respuesta.getMontoTotal() : 0);

            return respuesta;

        } catch (RestClientException e) {
            // TesoreriaService no está disponible (apagado, error de red, etc.)
            // En PROTOTIPO APF3: retornamos respuesta stub "sin deuda" para continuar el flujo
            // En PRODUCCIÓN: aquí iría Circuit Breaker (Resilience4j) con fallback adecuado
            log.warn("⚠️ [SOA] TesoreriaService no disponible en {}. Usando respuesta STUB (sin deuda).", url);
            log.warn("⚠️ Error: {}", e.getMessage());

            // Stub: simula que el estudiante NO tiene deudas
            return new DeudaResponse(false, 0, 0.0, "STUB: TesoreriaService no disponible");
        }
    }
}
