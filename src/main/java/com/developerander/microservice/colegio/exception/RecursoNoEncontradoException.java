package com.developerander.microservice.colegio.exception;

// ══════════════════════════════════════════════════════════════════════
// EXCEPCIÓN: RecursoNoEncontradoException
//
// Se lanza cuando un recurso (matrícula) no existe en la BD.
// Reemplaza el Optional.empty() manual en el controller:
//   Antes: return matriService.getById(id).orElse(404)
//   Ahora: lanza esta excepción → GlobalExceptionHandler → 404
// ══════════════════════════════════════════════════════════════════════
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String recurso, String campo, Object valor) {
        // Genera un mensaje descriptivo: "Matricula no encontrada con id: abc-123"
        super(String.format("%s no encontrado/a con %s: %s", recurso, campo, valor));
    }
}
