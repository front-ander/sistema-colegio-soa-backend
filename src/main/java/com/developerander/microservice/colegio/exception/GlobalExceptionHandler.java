package com.developerander.microservice.colegio.exception;

import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private Map<String, Object> buildErrorBody(String codigo, String mensaje, int status, String error) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("codigo", codigo);
        body.put("mensaje", mensaje);
        body.put("correlationId", MDC.get("correlationId"));
        body.put("status", status);
        body.put("error", error);
        body.put("timestamp", LocalDateTime.now().toString());
        return body;
    }

    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<Map<String, Object>> handleReglaNegocio(ReglaNegocioException ex) {
        HttpStatus status = HttpStatus.resolve(ex.getHttpStatus());
        if (status == null) status = HttpStatus.UNPROCESSABLE_ENTITY;

        return ResponseEntity
                .status(status)
                .body(buildErrorBody(ex.getCodigo(), ex.getMessage(), status.value(), status.getReasonPhrase()));
    }

    @ExceptionHandler(DeudaPendienteException.class)
    public ResponseEntity<Map<String, Object>> handleDeudaPendiente(DeudaPendienteException ex) {
        return ResponseEntity
                .status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(buildErrorBody("ERR-RN01", ex.getMessage(), 422, "Unprocessable Entity"));
    }

    @ExceptionHandler(SinVacantesException.class)
    public ResponseEntity<Map<String, Object>> handleSinVacantes(SinVacantesException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(buildErrorBody("ERR-RN02", ex.getMessage(), 409, "Conflict"));
    }

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<Map<String, Object>> handleRecursoNoEncontrado(RecursoNoEncontradoException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(buildErrorBody("ERR-404", ex.getMessage(), 404, "Not Found"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidacion(MethodArgumentNotValidException ex) {
        StringBuilder errores = new StringBuilder();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errores.append(error.getField()).append(": ").append(error.getDefaultMessage()).append("; ")
        );
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(buildErrorBody("ERR-400", errores.toString(), 400, "Bad Request"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenerico(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(buildErrorBody("ERR-500", "Error interno: " + ex.getMessage(), 500, "Internal Server Error"));
    }
}
