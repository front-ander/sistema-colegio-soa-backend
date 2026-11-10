package com.developerander.microservice.colegio.service.impl;

import com.developerander.microservice.colegio.dto.EstudianteResponseDTO;
import com.developerander.microservice.colegio.entity.MatriEntity;
import com.developerander.microservice.colegio.exception.EstudianteNoAptoException;
import com.developerander.microservice.colegio.repository.MatriRepository;
import com.developerander.microservice.colegio.service.MatriculaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
public class MatriculaServiceImpl implements MatriculaService {

    private final MatriRepository matriRepository;
    private final RestTemplate restTemplate;
    private final String estudianteBaseUrl;

    public MatriculaServiceImpl(
            MatriRepository matriRepository,
            RestTemplate restTemplate,
            @Value("${servicio.estudiantes.url}") String estudianteBaseUrl) {
        this.matriRepository = matriRepository;
        this.restTemplate = restTemplate;
        this.estudianteBaseUrl = estudianteBaseUrl;
    }

    @Override
    public MatriEntity registrarMatricula(MatriEntity matriEntity) {
        String dni = matriEntity.getDni();
        if (dni == null || dni.trim().isEmpty()) {
            throw new EstudianteNoAptoException("El estudiante no está apto para matricularse: debe ingresar un DNI válido");
        }

        String dniLimpio = dni.trim();
        String url = estudianteBaseUrl.endsWith("/")
                ? estudianteBaseUrl + dniLimpio
                : estudianteBaseUrl + "/" + dniLimpio;

        log.info("Consultando EstudianteService en {} para validar DNI {}", url, dniLimpio);

        EstudianteResponseDTO estudiante;
        try {
            ResponseEntity<EstudianteResponseDTO> response = restTemplate.getForEntity(url, EstudianteResponseDTO.class);
            estudiante = response.getBody();
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Estudiante con DNI {} no encontrado en EstudianteService (404)", dniLimpio);
            throw new EstudianteNoAptoException("El estudiante no está apto para matricularse: no existe estudiante con el DNI " + dniLimpio);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                log.warn("Estudiante con DNI {} no encontrado en EstudianteService (404)", dniLimpio);
                throw new EstudianteNoAptoException("El estudiante no está apto para matricularse: no existe estudiante con el DNI " + dniLimpio);
            }
            log.error("Error al consultar EstudianteService con status {}: {}", e.getStatusCode(), e.getMessage());
            throw new EstudianteNoAptoException("El estudiante no está apto para matricularse: error al consultar el servicio de estudiantes");
        } catch (RestClientException e) {
            log.error("Fallo de comunicación con EstudianteService: {}", e.getMessage());
            throw new EstudianteNoAptoException("El estudiante no está apto para matricularse: no se pudo comunicar con el servicio de estudiantes");
        }

        if (estudiante == null || estudiante.getEstado() == null || !"ACTIVO".equalsIgnoreCase(estudiante.getEstado().trim())) {
            String estadoActual = (estudiante != null && estudiante.getEstado() != null)
                    ? estudiante.getEstado()
                    : "DESCONOCIDO";
            log.warn("Estudiante DNI {} no esta apto: estado es {}", dniLimpio, estadoActual);
            throw new EstudianteNoAptoException("El estudiante no está apto para matricularse: su estado actual es " + estadoActual + " y debe ser ACTIVO");
        }

        // Asociar DNI e ID del estudiante a la matrícula
        matriEntity.setEstudianteId(estudiante.getId());
        matriEntity.setDni(estudiante.getDni());

        if (matriEntity.getNombreAlumno() == null || matriEntity.getNombreAlumno().trim().isEmpty()) {
            matriEntity.setNombreAlumno(estudiante.getNombres());
        }
        if (matriEntity.getFechaMatricula() == null) {
            matriEntity.setFechaMatricula(LocalDate.now());
        }
        if (matriEntity.getPagoTotal() == null) {
            matriEntity.setPagoTotal(BigDecimal.ZERO);
        }

        MatriEntity guardada = matriRepository.save(matriEntity);
        log.info("Matrícula registrada exitosamente con ID {} para estudiante ID {} (DNI {})",
                guardada.getId(), guardada.getEstudianteId(), guardada.getDni());

        return guardada;
    }

    @Override
    public List<MatriEntity> listarMatriculas() {
        return matriRepository.findAll();
    }
}
