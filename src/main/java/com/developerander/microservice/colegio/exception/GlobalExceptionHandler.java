package com.developerander.microservice.colegio.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

// ══════════════════════════════════════════════════════════════════════
// MANEJADOR GLOBAL DE EXCEPCIONES
//
// @RestControllerAdvice = Intercepta excepciones lanzadas por CUALQUIER
// @RestController del proyecto y las convierte en respuestas JSON.
//
// Sin esto: Spring devuelve HTML de error (ugly, poco informativo)
// Con esto:  Devuelve JSON estructurado con timestamp, código y mensaje
//
// Principio SOA: las respuestas de error también deben ser contratos
// estables y documentados (mismo formato siempre)
// ══════════════════════════════════════════════════════════════════════
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ── Método auxiliar: construye el cuerpo JSON estándar de error ───
    private Map<String, Object> buildErrorBody(HttpStatus status, String error, String mensaje) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString()); // Cuándo ocurrió el error
        body.put("status", status.value());                    // Código HTTP numérico (422, 404...)
        body.put("error", error);                              // Nombre del error
        body.put("mensaje", mensaje);                          // Descripción legible
        return body;
    }

    // ── DEUDA PENDIENTE → 422 Unprocessable Entity ────────────────────
    // 422 = La petición es sintácticamente válida pero no se puede procesar
    // por una regla de negocio (el estudiante debe pagar primero)
    @ExceptionHandler(DeudaPendienteException.class)
    public ResponseEntity<Map<String, Object>> handleDeudaPendiente(DeudaPendienteException ex) {
        return ResponseEntity
                .status(HttpStatus.UNPROCESSABLE_ENTITY)       // 422
                .body(buildErrorBody(
                        HttpStatus.UNPROCESSABLE_ENTITY,
                        "Deuda Pendiente",
                        ex.getMessage()
                ));
    }

    // ── SIN VACANTES → 422 Unprocessable Entity ───────────────────────
    @ExceptionHandler(SinVacantesException.class)
    public ResponseEntity<Map<String, Object>> handleSinVacantes(SinVacantesException ex) {
        return ResponseEntity
                .status(HttpStatus.UNPROCESSABLE_ENTITY)       // 422
                .body(buildErrorBody(
                        HttpStatus.UNPROCESSABLE_ENTITY,
                        "Sin Vacantes",
                        ex.getMessage()
                ));
    }

    // ── RECURSO NO ENCONTRADO → 404 Not Found ─────────────────────────
    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<Map<String, Object>> handleRecursoNoEncontrado(RecursoNoEncontradoException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)                  // 404
                .body(buildErrorBody(
                        HttpStatus.NOT_FOUND,
                        "Recurso No Encontrado",
                        ex.getMessage()
                ));
    }

    // ── VALIDACIÓN (@Valid) → 400 Bad Request ─────────────────────────
    // Se activa cuando @Valid detecta campos @NotNull vacíos u otras violaciones
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidacion(MethodArgumentNotValidException ex) {
        // Recopila TODOS los errores de validación (puede haber varios campos inválidos)
        StringBuilder errores = new StringBuilder();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errores.append(error.getField())             // Nombre del campo
                       .append(": ")
                       .append(error.getDefaultMessage())    // Mensaje del @NotNull, etc.
                       .append("; ")
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)               // 400
                .body(buildErrorBody(
                        HttpStatus.BAD_REQUEST,
                        "Datos Inválidos",
                        errores.toString()
                ));
    }

    // ── ERROR GENÉRICO → 500 Internal Server Error ────────────────────
    // Captura cualquier excepción no manejada específicamente
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenerico(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)      // 500
                .body(buildErrorBody(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Error Interno",
                        "Contacte al administrador: " + ex.getMessage()
                ));
    }
}
