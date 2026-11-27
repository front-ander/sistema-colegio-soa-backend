package com.developerander.microservice.colegio.orchestrator;

import com.developerander.microservice.colegio.client.TesoreriaClient;
import com.developerander.microservice.colegio.dto.DeudaResponse;
import com.developerander.microservice.colegio.entity.Aula;
import com.developerander.microservice.colegio.entity.EstadoMatricula;
import com.developerander.microservice.colegio.entity.MatriEntity;
import com.developerander.microservice.colegio.event.MatriculaEventPublisher;
import com.developerander.microservice.colegio.event.MatriculaRegistradaEvent;
import com.developerander.microservice.colegio.exception.RecursoNoEncontradoException;
import com.developerander.microservice.colegio.exception.ReglaNegocioException;
import com.developerander.microservice.colegio.repository.AulaRepository;
import com.developerander.microservice.colegio.repository.MatriRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
public class ProcesoMatricula {

        private static final Logger log = LoggerFactory.getLogger(ProcesoMatricula.class);

    @Autowired
    private TesoreriaClient tesoreriaClient;

    @Autowired
    private AulaRepository aulaRepository;

    @Autowired
    private MatriRepository matriRepository;

    @Autowired
    private MatriculaEventPublisher eventPublisher;

    @Transactional
    public MatriEntity ejecutar(MatriEntity matricula, String correlationId) {

                log.info("🚀 [BPM] Iniciando ProcesoMatricula | CorrelationId: {}", correlationId);

        // 1. Validar duplicados (Idempotencia)
        if (matriRepository.existsByIdEstudianteAndIdAulaAndEstado(
                matricula.getIdEstudiante(), matricula.getIdAula(), EstadoMatricula.REGISTRADA)) {
            log.warn("❌ [ERR-DUP] El estudiante {} ya está matriculado en el aula {}",
                    matricula.getIdEstudiante(), matricula.getIdAula());
            throw new ReglaNegocioException("ERR-DUP",
                    "El estudiante ya tiene una matrícula activa en esta aula", 409);
        }

        // 2. Validar vacantes con bloqueo pesimista (SELECT ... FOR UPDATE)
        Aula aula = aulaRepository.findByIdForUpdate(matricula.getIdAula())
                .orElseThrow(() -> new RecursoNoEncontradoException("Aula", "id", matricula.getIdAula()));

        if (aula.getVacantes() <= 0) {
            log.warn("❌ [ERR-RN02] Aula {} ({} {}) sin vacantes disponibles",
                    aula.getIdAula(), aula.getGrado(), aula.getSeccion());
            throw new ReglaNegocioException("ERR-RN02",
                    "No hay vacantes disponibles en esta aula", 409);
        }

        // 3. Validar deudas reales en TesoreriaService (http://localhost:8081)
        DeudaResponse deuda = tesoreriaClient.consultarDeudas(matricula.getIdEstudiante());
        if (deuda != null && deuda.isTieneDeuda()) {
            log.warn("❌ [ERR-RN01] Estudiante {} registra deuda pendiente en Tesorería",
                    matricula.getIdEstudiante());
            throw new ReglaNegocioException("ERR-RN01",
                    "El estudiante registra deuda pendiente en Tesorería", 422);
        }

        // 4. Generar cargo real en TesoreriaService antes de guardar la matrícula
        UUID idCargoGenerado = tesoreriaClient.generarCargo(matricula.getIdEstudiante(), matricula.getIdCargo());
        matricula.setIdCargo(idCargoGenerado);
        matricula.setEstado(EstadoMatricula.REGISTRADA);

        // 5. Persistir matrícula (con compensación Saga si falla o si simularFallo == true)
        MatriEntity matriculaGuardada;
        try {
            if (matricula.isSimularFallo()) {
                throw new RuntimeException("Fallo simulado en el registro de matrícula para probar compensación Saga");
            }
            matriculaGuardada = matriRepository.save(matricula);
        } catch (Exception ex) {
            log.error("💥 [SAGA] Error guardando matrícula. Ejecutando compensación en TesoreriaService para cargo {}",
                    idCargoGenerado);
            tesoreriaClient.anularCargo(idCargoGenerado, "Compensación automática por fallo en registro de matrícula");
            throw new ReglaNegocioException("ERR-SAGA",
                    "No se pudo completar el registro de matrícula; el cargo generado fue anulado por compensación", 422);
        }

        // 6. Descontar 1 vacante del aula y guardar en MySQL
        aula.setVacantes(aula.getVacantes() - 1);
        aulaRepository.save(aula);
        log.info("🏫 [BD-AULAS] Vacante descontada. Aula {} ahora tiene {} vacante(s).",
                aula.getIdAula(), aula.getVacantes());

        // 7. Publicar evento asíncrono a RabbitMQ
        MatriculaRegistradaEvent evento = new MatriculaRegistradaEvent(
                matriculaGuardada.getIdMatricula(),
                matriculaGuardada.getIdEstudiante(),
                matriculaGuardada.getFechaMatricula(),
                matriculaGuardada.getEstado().name(),
                correlationId,
                null
        );
        eventPublisher.publicarMatriculaRegistrada(evento);

        log.info("🏁 [BPM] ProcesoMatricula completado | ID: {}", matriculaGuardada.getIdMatricula());
        return matriculaGuardada;
    }
}
