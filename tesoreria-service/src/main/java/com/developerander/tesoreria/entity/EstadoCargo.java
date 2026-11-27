package com.developerander.tesoreria.entity;

// Estados posibles de un cargo en colegio_tesoreria_db.cargos
public enum EstadoCargo {
    PENDIENTE,             // Cargo generado al matricular, pendiente de pago
    PAGADO,                // Cargo cancelado en caja/banco
    ANULADO_COMPENSACION   // Cargo revertido automáticamente por compensación Saga si falla la matrícula
}
