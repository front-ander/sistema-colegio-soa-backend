package com.developerander.microservice.colegio.client;

import com.developerander.microservice.colegio.dto.DeudaResponse;
import com.developerander.microservice.colegio.exception.ReglaNegocioException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@Component
public class TesoreriaClient {

    private static final Logger log = LoggerFactory.getLogger(TesoreriaClient.class);

    @Autowired
    private RestTemplate restTemplate;

    @Value("${servicios.tesoreria.url:http://localhost:8081}")
    private String tesoreriaUrl;

    private HttpHeaders buildPropagatedHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String corrId = MDC.get("correlationId");
        if (corrId != null && !corrId.isBlank()) {
            headers.set("X-Correlation-ID", corrId);
        }

        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            HttpServletRequest req = attrs.getRequest();
            String auth = req.getHeader("Authorization");
            if (auth != null && !auth.isBlank()) {
                headers.set("Authorization", auth);
            }
        }
        return headers;
    }

    public DeudaResponse consultarDeudas(UUID idEstudiante) {
        String url = tesoreriaUrl + "/api/v1/deudas/" + idEstudiante;
        try {
            log.info("🔗 [SOA] GET {}", url);
            HttpEntity<Void> entity = new HttpEntity<>(buildPropagatedHeaders());
            ResponseEntity<DeudaResponse> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, DeudaResponse.class);
            return response.getBody();
        } catch (HttpStatusCodeException e) {
            log.error("❌ [SOA] TesoreriaService respondió error HTTP {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new ReglaNegocioException("ERR-TES-HTTP",
                    "Error al consultar deudas en TesoreriaService: " + e.getStatusCode(), 503);
        } catch (RestClientException e) {
            log.error("🚫 [SOA] TesoreriaService (8081) no disponible: {}", e.getMessage());
            throw new ReglaNegocioException("ERR-TES-503",
                    "No se pudo verificar el estado financiero: TesoreriaService no está disponible", 503);
        }
    }

    public UUID generarCargo(UUID idEstudiante, UUID idCargoSugerido) {
        String url = tesoreriaUrl + "/api/v1/cargos";
        try {
            log.info("🔗 [SOA] POST {} | Estudiante: {}", url, idEstudiante);
            Map<String, Object> body = Map.of(
                    "idEstudiante", idEstudiante.toString(),
                    "concepto", "Matrícula 2026",
                    "monto", 300.00,
                    "fechaVencimiento", LocalDate.now().plusDays(15).toString()
            );
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, buildPropagatedHeaders());
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, new ParameterizedTypeReference<>() {});
            Map<String, Object> respBody = response.getBody();
            if (respBody != null && respBody.get("idCargo") != null) {
                return UUID.fromString(respBody.get("idCargo").toString());
            }
            return idCargoSugerido != null ? idCargoSugerido : UUID.randomUUID();
        } catch (RestClientException e) {
            log.error("🚫 [SOA] Error al generar cargo en TesoreriaService: {}", e.getMessage());
            throw new ReglaNegocioException("ERR-TES-503",
                    "No se pudo generar el cargo en TesoreriaService", 503);
        }
    }

    public void anularCargo(UUID idCargo, String motivo) {
        String url = tesoreriaUrl + "/api/v1/cargos/" + idCargo + "/anular";
        try {
            log.warn("🔄 [SAGA-COMPENSACIÓN] PATCH {} | Motivo: {}", url, motivo);
            Map<String, String> body = Map.of("motivo", motivo);
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, buildPropagatedHeaders());
            restTemplate.exchange(url, HttpMethod.PATCH, entity, Void.class);
            log.info("✅ [SAGA-COMPENSACIÓN] Cargo {} anulado exitosamente.", idCargo);
        } catch (RestClientException e) {
            log.error("❌ [SAGA-COMPENSACIÓN] Falló al anular el cargo {}: {}", idCargo, e.getMessage());
        }
    }
}
