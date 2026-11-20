package com.developerander.microservice.colegio.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

// ══════════════════════════════════════════════════════════════════════
// DTO: DeudaResponse
//
// Data Transfer Object que representa la respuesta que devuelve
// TesoreriaService cuando se consultan las deudas de un estudiante.
//
// DTO = objeto solo para transferir datos entre servicios.
// NO se guarda en base de datos (no tiene @Entity).
//
// En el flujo SOA:
//   ProcesoMatricula → TesoreriaClient → TesoreriaService
//   TesoreriaService responde con este JSON → se deserializa aquí
// ══════════════════════════════════════════════════════════════════════
@Data               // Lombok: genera getters, setters, toString, equals
@AllArgsConstructor // Lombok: genera constructor con todos los campos
@NoArgsConstructor  // Lombok: genera constructor vacío (necesario para deserializar JSON)
public class DeudaResponse {

    // true = el estudiante tiene al menos una deuda activa sin pagar
    // false = no tiene deudas → puede matricularse
    private boolean tieneDeuda;

    // Número total de cuotas pendientes de pago
    private int cantidadDeudas;

    // Monto total adeudado en soles
    private double montoTotal;

    // Descripción textual del estado de deuda
    private String detalle;
}
