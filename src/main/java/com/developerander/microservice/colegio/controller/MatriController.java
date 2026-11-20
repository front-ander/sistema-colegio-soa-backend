package com.developerander.microservice.colegio.controller;

// ── Importaciones de la capa de negocio ──────────────────────────────
import com.developerander.microservice.colegio.entity.EstadoMatricula;
import com.developerander.microservice.colegio.entity.MatriEntity;
import com.developerander.microservice.colegio.service.MatriService;

// ── Anotaciones de Swagger/OpenAPI para documentación ────────────────
import io.swagger.v3.oas.annotations.Operation;         // Describe qué hace cada endpoint
import io.swagger.v3.oas.annotations.Parameter;         // Describe parámetros individuales
import io.swagger.v3.oas.annotations.media.Content;     // Define el tipo de contenido (JSON)
import io.swagger.v3.oas.annotations.media.Schema;      // Define el schema del body
import io.swagger.v3.oas.annotations.responses.ApiResponse;  // Documenta posibles respuestas HTTP
import io.swagger.v3.oas.annotations.responses.ApiResponses; // Agrupa múltiples ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag;          // Agrupa endpoints en Swagger UI

// ── Anotaciones Spring para manejo HTTP ──────────────────────────────
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;         // Wrapper para controlar código HTTP + body
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;                        // Activa las validaciones de @NotNull, etc.

import java.util.List;
import java.util.UUID;

// ══════════════════════════════════════════════════════════════════════
// CONTROLLER: MatriController
//
// La capa de CONTROLLER maneja las peticiones HTTP entrantes:
//   1. Recibe la petición (URL, método HTTP, body, path variables)
//   2. Llama al SERVICE para ejecutar la lógica de negocio
//   3. Devuelve la respuesta HTTP con el código de estado apropiado
//
// @RestController = @Controller + @ResponseBody
//   → Cada método devuelve JSON directamente (sin plantillas HTML)
//
// @RequestMapping = Prefijo de URL para TODOS los endpoints de este controller
//   → Todas las rutas empiezan con /api/matriculas
//
// @Tag = Agrupa estos endpoints en Swagger UI bajo el nombre "Matrículas"
// ══════════════════════════════════════════════════════════════════════
@RestController
// Versionado de API según estándar APF2 Tabla 14: prefijo /api/v1/
// Versionar la ruta permite evolucionar la API sin romper clientes existentes.
// v1 = primera versión pública del servicio de matrículas.
@RequestMapping("/api/v1/matriculas")
@Validated                          // Activa validación de parámetros en métodos del controller
@Tag(name = "Matrículas", description = "CRUD completo para gestión de matrículas escolares")
public class MatriController {

    // ── INYECCIÓN DE DEPENDENCIA ──────────────────────────────────────
    // @Autowired = Spring crea e inyecta automáticamente el MatriService
    // El controller NO crea instancias directamente (Inversión de Control)
    @Autowired
    private MatriService matriService;

    // ══════════════════════════════════════════════════════════════════
    // GET /api/matriculas
    // Devuelve TODAS las matrículas registradas en la base de datos
    // HTTP 200 OK con lista JSON (puede ser lista vacía [])
    // ══════════════════════════════════════════════════════════════════
    @Operation(
        summary = "Listar todas las matrículas",
        description = "Retorna la lista completa de matrículas. Si no hay registros, retorna lista vacía []."
    )
    @ApiResponse(responseCode = "200", description = "Lista de matrículas obtenida exitosamente")
    @GetMapping                             // Mapea: GET /api/matriculas
    @ResponseStatus(HttpStatus.OK)          // Siempre devuelve 200
    public List<MatriEntity> getAllMatriculas() {
        return matriService.getAllMatriculas(); // Delega al service
    }

    // ══════════════════════════════════════════════════════════════════
    // GET /api/matriculas/{id}
    // Busca UNA matrícula por su UUID
    // HTTP 200 si existe | HTTP 404 si no existe
    // ══════════════════════════════════════════════════════════════════
    @Operation(
        summary = "Buscar matrícula por ID",
        description = "Busca una matrícula específica usando su UUID. Retorna 404 si no existe."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Matrícula encontrada"),
        @ApiResponse(responseCode = "404", description = "Matrícula no encontrada", content = @Content)
    })
    @GetMapping("/{id}")                    // Mapea: GET /api/matriculas/{id}
    public ResponseEntity<MatriEntity> getMatriculaById(
            // @PathVariable extrae el {id} de la URL y lo convierte a UUID
            @Parameter(description = "UUID de la matrícula", required = true)
            @PathVariable UUID id) {

        // matriService.getMatriculaById() devuelve Optional<MatriEntity>
        // .map(ResponseEntity::ok) → si existe, envuelve en 200 OK
        // .orElse(notFound)        → si no existe, devuelve 404
        return matriService.getMatriculaById(id)
                .map(ResponseEntity::ok)                        // 200 OK + body con la matrícula
                .orElse(ResponseEntity.notFound().build());     // 404 Not Found sin body
    }

    // ══════════════════════════════════════════════════════════════════
    // POST /api/matriculas
    // Crea una NUEVA matrícula
    // HTTP 201 CREATED con la entidad guardada (incluye el UUID generado)
    // ══════════════════════════════════════════════════════════════════
    @Operation(
        summary = "Crear nueva matrícula",
        description = "Registra una nueva matrícula. El estado se establece automáticamente como REGISTRADA."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Matrícula creada exitosamente",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = MatriEntity.class))),
        @ApiResponse(responseCode = "400", description = "Datos inválidos en el body", content = @Content)
    })
    @PostMapping                            // Mapea: POST /api/matriculas
    @ResponseStatus(HttpStatus.CREATED)     // Devuelve 201 Created (correcto para POST)
    public MatriEntity createMatricula(
            // @RequestBody = Lee el JSON del body de la petición y lo convierte a MatriEntity
            // @Valid = Activa las validaciones @NotNull definidas en la entidad
            @Valid @RequestBody MatriEntity matriEntity) {

        return matriService.createMatricula(matriEntity); // Service asigna estado REGISTRADA y guarda
    }

    // ══════════════════════════════════════════════════════════════════
    // PUT /api/matriculas/{id}
    // Actualiza COMPLETAMENTE una matrícula existente
    // HTTP 200 si actualizó | HTTP 404 si no existe
    // ══════════════════════════════════════════════════════════════════
    @Operation(
        summary = "Actualizar matrícula",
        description = "Actualiza todos los campos de una matrícula existente. Retorna 404 si el ID no existe."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Matrícula actualizada exitosamente"),
        @ApiResponse(responseCode = "404", description = "Matrícula no encontrada", content = @Content),
        @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content)
    })
    @PutMapping("/{id}")                    // Mapea: PUT /api/matriculas/{id}
    public ResponseEntity<MatriEntity> updateMatricula(
            @Parameter(description = "UUID de la matrícula a actualizar", required = true)
            @PathVariable UUID id,          // ID extraído de la URL
            @Valid @RequestBody MatriEntity datosNuevos) { // Nuevos datos del body JSON

        // El service devuelve Optional<MatriEntity>:
        //   - Con valor → matrícula actualizada → 200 OK
        //   - Vacío     → no existe ese ID → 404 Not Found
        return matriService.updateMatricula(id, datosNuevos)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ══════════════════════════════════════════════════════════════════
    // DELETE /api/matriculas/{id}
    // Elimina una matrícula de la base de datos
    // HTTP 204 No Content si borró | HTTP 404 si no existe
    // ══════════════════════════════════════════════════════════════════
    @Operation(
        summary = "Eliminar matrícula",
        description = "Elimina permanentemente una matrícula. Retorna 404 si el ID no existe."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Matrícula eliminada exitosamente"),
        @ApiResponse(responseCode = "404", description = "Matrícula no encontrada", content = @Content)
    })
    @DeleteMapping("/{id}")                 // Mapea: DELETE /api/matriculas/{id}
    public ResponseEntity<Void> deleteMatricula(
            @Parameter(description = "UUID de la matrícula a eliminar", required = true)
            @PathVariable UUID id) {

        if (matriService.deleteMatricula(id)) {
            // true → el service pudo borrarlo → 204 No Content (correcto para DELETE exitoso)
            return ResponseEntity.noContent().build();
        }
        // false → no existía ese ID → 404 Not Found
        return ResponseEntity.notFound().build();
    }

    // ══════════════════════════════════════════════════════════════════
    // GET /api/matriculas/estado/{estado}
    // Filtra matrículas por estado (REGISTRADA o ANULADA)
    // HTTP 200 con lista (puede ser vacía)
    // ══════════════════════════════════════════════════════════════════
    @Operation(
        summary = "Listar matrículas por estado",
        description = "Filtra matrículas según su estado: REGISTRADA o ANULADA."
    )
    @ApiResponse(responseCode = "200", description = "Lista filtrada por estado")
    @GetMapping("/estado/{estado}")         // Mapea: GET /api/matriculas/estado/REGISTRADA
    public ResponseEntity<List<MatriEntity>> getMatriculasByEstado(
            @Parameter(description = "Estado de la matrícula: REGISTRADA | ANULADA", required = true)
            @PathVariable EstadoMatricula estado) { // Spring convierte "REGISTRADA" al enum automáticamente

        List<MatriEntity> resultado = matriService.getMatriculasByEstado(estado);
        return ResponseEntity.ok(resultado); // Siempre 200, aunque la lista esté vacía
    }

    // ══════════════════════════════════════════════════════════════════
    // GET /api/matriculas/estudiante/{idEstudiante}
    // Busca todas las matrículas de un estudiante específico
    // ══════════════════════════════════════════════════════════════════
    @Operation(
        summary = "Listar matrículas por estudiante",
        description = "Retorna todas las matrículas asociadas a un UUID de estudiante."
    )
    @ApiResponse(responseCode = "200", description = "Lista de matrículas del estudiante")
    @GetMapping("/estudiante/{idEstudiante}") // Mapea: GET /api/matriculas/estudiante/{uuid}
    public ResponseEntity<List<MatriEntity>> getMatriculasByEstudiante(
            @Parameter(description = "UUID del estudiante", required = true)
            @PathVariable UUID idEstudiante) {

        List<MatriEntity> resultado = matriService.getMatriculasByEstudiante(idEstudiante);
        return ResponseEntity.ok(resultado);
    }

}
