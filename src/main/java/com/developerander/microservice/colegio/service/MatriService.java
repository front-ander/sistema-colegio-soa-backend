package com.developerander.microservice.colegio.service;

import com.developerander.microservice.colegio.entity.EstadoMatricula;
import com.developerander.microservice.colegio.entity.MatriEntity;
import com.developerander.microservice.colegio.exception.RecursoNoEncontradoException;
import com.developerander.microservice.colegio.filter.CorrelationIdFilter;
import com.developerander.microservice.colegio.orchestrator.ProcesoMatricula;
import com.developerander.microservice.colegio.repository.MatriRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

// ══════════════════════════════════════════════════════════════════════
// SERVICE: MatriService (actualizado con orquestación SOA)
//
// Cambio clave respecto a la versión anterior:
//   ANTES: createMatricula() hacía directamente matriRepository.save()
//   AHORA: createMatricula() llama a ProcesoMatricula.ejecutar()
//          que implementa el flujo BPM completo:
//          1. Verifica deudas → 2. Verifica vacantes → 3. Guarda → 4. Publica evento
// ══════════════════════════════════════════════════════════════════════
@Service
public class MatriService {

    @Autowired
    private MatriRepository matriRepository;

    // Orquestador BPM: ejecuta el proceso completo de matrícula
    @Autowired
    private ProcesoMatricula procesoMatricula;

    // ── LISTAR TODAS ──────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<MatriEntity> getAllMatriculas() {
        return matriRepository.findAll();
    }

    // ── BUSCAR POR ID ─────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public Optional<MatriEntity> getMatriculaById(UUID id) {
        return matriRepository.findById(id);
    }

    // ── CREAR — ahora delega al orquestador SOA ───────────────────────
    @Transactional
    public MatriEntity createMatricula(MatriEntity matriEntity) {
        // Extrae el X-Correlation-ID que el CorrelationIdFilter guardó en la request
        // Permite trazar esta petición a través de todos los microservicios
        String correlationId = extraerCorrelationId();

        // Delega al orquestador: ejecuta el proceso BPM completo
        // Si hay deuda o sin vacantes → ProcesoMatricula lanza excepción
        // → GlobalExceptionHandler → HTTP 422 con mensaje descriptivo
        return procesoMatricula.ejecutar(matriEntity, correlationId);
    }

    // ── ACTUALIZAR ────────────────────────────────────────────────────
    @Transactional
    public Optional<MatriEntity> updateMatricula(UUID id, MatriEntity datosNuevos) {
        return matriRepository.findById(id).map(existente -> {
            existente.setIdEstudiante(datosNuevos.getIdEstudiante());
            existente.setIdCargo(datosNuevos.getIdCargo());
            existente.setIdAula(datosNuevos.getIdAula());
            existente.setFechaMatricula(datosNuevos.getFechaMatricula());
            existente.setEstado(datosNuevos.getEstado());
            return matriRepository.save(existente);
        });
    }

    // ── ELIMINAR ──────────────────────────────────────────────────────
    @Transactional
    public boolean deleteMatricula(UUID id) {
        if (matriRepository.existsById(id)) {
            matriRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // ── BUSCAR POR ESTADO ─────────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<MatriEntity> getMatriculasByEstado(EstadoMatricula estado) {
        return matriRepository.findByEstado(estado);
    }

    // ── BUSCAR POR ESTUDIANTE ─────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<MatriEntity> getMatriculasByEstudiante(UUID idEstudiante) {
        return matriRepository.findByIdEstudiante(idEstudiante);
    }

    // ── MÉTODO AUXILIAR: leer el Correlation-ID de la petición actual ─
    private String extraerCorrelationId() {
        try {
            // RequestContextHolder permite acceder a la request HTTP actual
            // desde cualquier clase de Spring (no solo desde el controller)
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                // Lee el atributo que CorrelationIdFilter guardó en doFilterInternal()
                Object corrId = request.getAttribute(CorrelationIdFilter.CORRELATION_ID_HEADER);
                if (corrId != null) return corrId.toString();
            }
        } catch (Exception e) {
            // Si por alguna razón no puede leer la request, usa un UUID nuevo
        }
        return UUID.randomUUID().toString(); // Fallback
    }
}
