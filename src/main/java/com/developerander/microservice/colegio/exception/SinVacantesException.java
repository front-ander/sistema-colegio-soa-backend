package com.developerander.microservice.colegio.exception;

// ══════════════════════════════════════════════════════════════════════
// EXCEPCIÓN: SinVacantesException
//
// Se lanza cuando el orquestador (ProcesoMatricula) consulta a
// AulaService y el aula seleccionada ya no tiene vacantes disponibles.
//
// En el flujo SOA:
//   ProcesoMatricula → AulaClient → AulaService
//   Si sin vacantes → lanza esta excepción
//   GlobalExceptionHandler la captura → devuelve HTTP 422
// ══════════════════════════════════════════════════════════════════════
public class SinVacantesException extends RuntimeException {

    public SinVacantesException(String message) {
        super(message);
    }
}
