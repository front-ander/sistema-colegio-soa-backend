package com.developerander.microservice.colegio.entity;

// ══════════════════════════════════════════════════════════════════════
// ENUM: EstadoMatricula
// Define los estados posibles que puede tener una matrícula.
// Se usa con @Enumerated(EnumType.STRING) en la entidad para que
// se guarde el TEXTO ("REGISTRADA") en MySQL, no un número (0, 1).
// ══════════════════════════════════════════════════════════════════════

public enum EstadoMatricula {

    REGISTRADA,  // La matrícula fue creada y está activa
    ANULADA      // La matrícula fue cancelada/anulada

}
