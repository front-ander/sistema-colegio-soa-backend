package com.developerander.microservice.colegio.orchestrator;

import com.developerander.microservice.colegio.client.AulaClient;
import com.developerander.microservice.colegio.client.TesoreriaClient;
import com.developerander.microservice.colegio.dto.DeudaResponse;
import com.developerander.microservice.colegio.dto.VacanteResponse;
import com.developerander.microservice.colegio.entity.EstadoMatricula;
import com.developerander.microservice.colegio.entity.MatriEntity;
import com.developerander.microservice.colegio.event.MatriculaEventPublisher;
import com.developerander.microservice.colegio.event.MatriculaRegistradaEvent;
import com.developerander.microservice.colegio.exception.DeudaPendienteException;
import com.developerander.microservice.colegio.exception.SinVacantesException;
import com.developerander.microservice.colegio.repository.MatriRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

// ══════════════════════════════════════════════════════════════════════
// ORQUESTADOR SOA: ProcesoMatricula
//
// Simula el motor de procesos BPMN de Flowable BPM.
// Implementa el proceso "ProcesoMatricula" definido en el APF2.
//
// En producción: Flowable ejecuta este proceso como un archivo .bpmn
// En el prototipo APF3: lo implementamos directamente en Java para
// demostrar la lógica de orquestación SOA.
//
// TAREAS DEL PROCESO (en orden):
//   Tarea 1 → Verificar deudas (TesoreriaService)
//   Tarea 2 → Verificar vacantes (AulaService)
//   Tarea 3 → Registrar matrícula (MatriRepository)
//   Tarea 4 → Publicar evento (RabbitMQ → NotificacionService)
//
// Si alguna tarea falla → lanza excepción → GlobalExceptionHandler → HTTP 422
// ══════════════════════════════════════════════════════════════════════
@Component
public class ProcesoMatricula {

    private static final Logger log = LoggerFactory.getLogger(ProcesoMatricula.class);

    @Autowired
    private TesoreriaClient tesoreriaClient;    // Tarea 1: consulta deudas

    @Autowired
    private AulaClient aulaClient;              // Tarea 2: consulta vacantes

    @Autowired
    private MatriRepository matriRepository;    // Tarea 3: guarda en BD

    @Autowired
    private MatriculaEventPublisher eventPublisher; // Tarea 4: publica evento

    // ── MÉTODO PRINCIPAL: ejecuta el proceso completo ─────────────────
    // @Transactional garantiza que si algo falla después del save(),
    // se hace ROLLBACK automático en MySQL (consistencia de datos)
    @Transactional
    public MatriEntity ejecutar(MatriEntity matricula, String correlationId) {

        log.info("🚀 [BPM] Iniciando ProcesoMatricula | CorrelationId: {}", correlationId);

        // ══════════════════════════════════════════════════
        // TAREA 1: VERIFICAR DEUDAS (Gateway exclusivo)
        // ══════════════════════════════════════════════════
        log.info("📋 [BPM - Tarea 1] Consultando deudas del estudiante: {}", matricula.getIdEstudiante());

        DeudaResponse deuda = tesoreriaClient.consultarDeudas(matricula.getIdEstudiante());

        if (deuda != null && deuda.isTieneDeuda()) {
            // Gateway de decisión: tiene deuda → proceso termina con error
            log.warn("❌ [BPM - Tarea 1] Estudiante {} tiene {} deuda(s) por S/. {}",
                    matricula.getIdEstudiante(),
                    deuda.getCantidadDeudas(),
                    deuda.getMontoTotal());

            // La excepción viaja hasta GlobalExceptionHandler → HTTP 422
            throw new DeudaPendienteException(
                    String.format("El estudiante tiene %d deuda(s) pendiente(s) por S/. %.2f. " +
                                  "Regularice su situación antes de matricularse.",
                                  deuda.getCantidadDeudas(), deuda.getMontoTotal())
            );
        }
        log.info("✅ [BPM - Tarea 1] Sin deudas. Continuando...");

        // ══════════════════════════════════════════════════
        // TAREA 2: VERIFICAR VACANTES (Gateway exclusivo)
        // ══════════════════════════════════════════════════
        log.info("📋 [BPM - Tarea 2] Consultando vacantes del aula: {}", matricula.getIdAula());

        VacanteResponse vacante = aulaClient.consultarVacantes(matricula.getIdAula());

        if (vacante != null && vacante.getVacantesDisponibles() <= 0) {
            // Gateway de decisión: sin vacantes → proceso termina con error
            log.warn("❌ [BPM - Tarea 2] Aula {} sin vacantes disponibles (capacidad: {}, matriculados: {})",
                    matricula.getIdAula(),
                    vacante != null ? vacante.getCapacidadMaxima() : "N/A",
                    vacante != null ? vacante.getMatriculados() : "N/A");

            throw new SinVacantesException(
                    String.format("El aula '%s' no tiene vacantes disponibles (Capacidad: %d, Ocupadas: %d).",
                                  vacante != null ? vacante.getNombreAula() : "seleccionada",
                                  vacante != null ? vacante.getCapacidadMaxima() : 0,
                                  vacante != null ? vacante.getMatriculados() : 0)
            );
        }
        log.info("✅ [BPM - Tarea 2] Vacantes disponibles: {}. Continuando...",
                vacante != null ? vacante.getVacantesDisponibles() : "N/A");

        // ══════════════════════════════════════════════════
        // TAREA 3: REGISTRAR MATRÍCULA EN BASE DE DATOS
        // ══════════════════════════════════════════════════
        log.info("📋 [BPM - Tarea 3] Registrando matrícula en MySQL...");

        // Asigna el estado inicial (regla de negocio: toda matrícula nueva es REGISTRADA)
        matricula.setEstado(EstadoMatricula.REGISTRADA);

        // Persiste en MySQL → Hibernate genera: INSERT INTO matricula (...)
        MatriEntity matriculaGuardada = matriRepository.save(matricula);

        log.info("✅ [BPM - Tarea 3] Matrícula registrada con ID: {}", matriculaGuardada.getIdMatricula());

        // ══════════════════════════════════════════════════
        // TAREA 4: PUBLICAR EVENTO ASÍNCRONO (Fire & Forget)
        // ══════════════════════════════════════════════════
        log.info("📋 [BPM - Tarea 4] Publicando evento a RabbitMQ...");

        // Construye el evento con todos los datos necesarios para NotificacionService
        MatriculaRegistradaEvent evento = new MatriculaRegistradaEvent(
                matriculaGuardada.getIdMatricula(),
                matriculaGuardada.getIdEstudiante(),
                matriculaGuardada.getFechaMatricula(),
                matriculaGuardada.getEstado().name(),
                correlationId,   // Para trazabilidad en los logs de NotificacionService
                null             // timestamp lo agrega el publisher
        );

        // Publica a RabbitMQ (asíncrono: no bloquea la respuesta al cliente)
        eventPublisher.publicarMatriculaRegistrada(evento);

        log.info("🏁 [BPM] ProcesoMatricula completado exitosamente | CorrelationId: {}", correlationId);

        return matriculaGuardada; // Devuelve la matrícula creada al controller
    }
}
