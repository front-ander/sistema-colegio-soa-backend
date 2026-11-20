package com.developerander.microservice.colegio.exception;

// ══════════════════════════════════════════════════════════════════════
// EXCEPCIÓN: DeudaPendienteException
//
// Se lanza cuando el orquestador (ProcesoMatricula) consulta a
// TesoreriaService y detecta que el estudiante tiene deudas activas.
//
// En el flujo SOA:
//   ProcesoMatricula → TesoreriaClient → TesoreriaService
//   Si tiene deuda → lanza esta excepción
//   GlobalExceptionHandler la captura → devuelve HTTP 422
// ══════════════════════════════════════════════════════════════════════
public class DeudaPendienteException extends RuntimeException {

    // RuntimeException no obliga al llamador a usar try-catch (unchecked)
    // Es la práctica recomendada en Spring para excepciones de negocio
    public DeudaPendienteException(String message) {
        super(message); // Pasa el mensaje al constructor de RuntimeException
    }
}
