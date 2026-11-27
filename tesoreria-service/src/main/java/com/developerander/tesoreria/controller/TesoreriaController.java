package com.developerander.tesoreria.controller;

import com.developerander.tesoreria.dto.CargoRequest;
import com.developerander.tesoreria.dto.DeudaResponse;
import com.developerander.tesoreria.entity.CargoEntity;
import com.developerander.tesoreria.entity.DeudaEntity;
import com.developerander.tesoreria.entity.EstadoCargo;
import com.developerander.tesoreria.repository.CargoRepository;
import com.developerander.tesoreria.repository.DeudaRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

// ══════════════════════════════════════════════════════════════════════
// CONTROLLER: TesoreriaController
// Expone las operaciones financieras de la Tabla 12 del APF2:
//   1. GET   /api/v1/deudas/{idEstudiante}  → Verifica deudas vencidas (RN-01)
//   2. POST  /api/v1/cargos                 → Genera un nuevo cargo de matrícula
//   3. PATCH /api/v1/cargos/{idCargo}/anular → Anula cargo por compensación Saga
// ══════════════════════════════════════════════════════════════════════
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/v1")
@Tag(name = "Tesorería", description = "Gestión de deudas, cargos y compensación Saga")
public class TesoreriaController {

    private static final Logger log = LoggerFactory.getLogger(TesoreriaController.class);

    @Autowired
    private DeudaRepository deudaRepository;

    @Autowired
    private CargoRepository cargoRepository;

    // ── 1. CONSULTAR DEUDAS VENCIDAS (RN-01) ──────────────────────────
    @Operation(summary = "Consultar deudas por estudiante",
               description = "Retorna tieneDeuda=true si el estudiante tiene cuotas vencidas sin pagar.")
    @GetMapping("/deudas/{idEstudiante}")
    public ResponseEntity<DeudaResponse> consultarDeudas(@PathVariable String idEstudiante) {
        log.info("💰 [TESORERIA] Consultando deudas vencidas para estudiante: {}", idEstudiante);

        // Consulta real en colegio_tesoreria_db.deudas
        List<DeudaEntity> vencidas = deudaRepository
                .findByIdEstudianteAndDeudaVencidaTrueAndPagadaFalse(idEstudiante);

        if (vencidas.isEmpty()) {
            log.info("✅ [TESORERIA] Estudiante {} sin deudas vencidas.", idEstudiante);
            return ResponseEntity.ok(new DeudaResponse(
                    false, 0, 0.0, "Estudiante al día en sus pagos"
            ));
        }

        // Suma el monto total adeudado
        double total = vencidas.stream()
                .mapToDouble(d -> d.getMonto().doubleValue())
                .sum();

        log.warn("⚠️ [TESORERIA - RN-01] Estudiante {} tiene {} deuda(s) vencida(s) por S/. {}",
                idEstudiante, vencidas.size(), total);

        return ResponseEntity.ok(new DeudaResponse(
                true,
                vencidas.size(),
                total,
                "Registra deuda(s) vencida(s) pendiente(s) de regularización"
        ));
    }

    // ── 2. GENERAR CARGO DE MATRÍCULA ─────────────────────────────────
    @Operation(summary = "Generar cargo de matrícula",
               description = "Registra un nuevo cargo en estado PENDIENTE y retorna su UUID.")
    @PostMapping("/cargos")
    public ResponseEntity<CargoEntity> generarCargo(@Valid @RequestBody CargoRequest request) {
        CargoEntity cargo = new CargoEntity();
        cargo.setIdCargo(UUID.randomUUID().toString()); // Genera UUID en formato CHAR(36)
        cargo.setIdEstudiante(request.getIdEstudiante());
        cargo.setConcepto(request.getConcepto());
        cargo.setMonto(request.getMonto());
        cargo.setFechaVencimiento(request.getFechaVencimiento());
        cargo.setEstado(EstadoCargo.PENDIENTE);

        CargoEntity guardado = cargoRepository.save(cargo);
        log.info("🧾 [TESORERIA] Cargo generado {} para estudiante {} por S/. {}",
                guardado.getIdCargo(), guardado.getIdEstudiante(), guardado.getMonto());

        return ResponseEntity.status(HttpStatus.CREATED).body(guardado);
    }

    // ── 3. ANULAR CARGO (COMPENSACIÓN SAGA) ───────────────────────────
    @Operation(summary = "Anular cargo por compensación Saga",
               description = "Cambia el estado del cargo a ANULADO_COMPENSACION si falla el registro de matrícula.")
    @PatchMapping("/cargos/{idCargo}/anular")
    public ResponseEntity<?> anularCargo(
            @PathVariable String idCargo,
            @RequestBody(required = false) Map<String, String> body) {

        String motivo = (body != null && body.get("motivo") != null)
                ? body.get("motivo")
                : "Anulado por compensación Saga";

        return cargoRepository.findById(idCargo)
                .map(cargo -> {
                    cargo.setEstado(EstadoCargo.ANULADO_COMPENSACION);
                    cargo.setMotivoAnulacion(motivo);
                    CargoEntity actualizado = cargoRepository.save(cargo);
                    log.warn("🔄 [TESORERIA - SAGA] Cargo {} anulado. Motivo: {}", idCargo, motivo);
                    return ResponseEntity.ok(actualizado);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
