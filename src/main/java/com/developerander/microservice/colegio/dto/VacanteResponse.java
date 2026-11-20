package com.developerander.microservice.colegio.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

// ══════════════════════════════════════════════════════════════════════
// DTO: VacanteResponse
//
// Respuesta que devuelve AulaService cuando se consultan
// las vacantes disponibles de un aula específica.
//
// En el flujo SOA:
//   ProcesoMatricula → AulaClient → AulaService
//   AulaService responde con este JSON → se deserializa aquí
// ══════════════════════════════════════════════════════════════════════
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VacanteResponse {

    // UUID del aula consultada (confirmación de identidad)
    private String idAula;

    // Nombre o código del aula (ej: "AULA-5A")
    private String nombreAula;

    // Capacidad máxima de estudiantes que caben en el aula
    private int capacidadMaxima;

    // Cuántos estudiantes ya están matriculados en esta aula
    private int matriculados;

    // Vacantes libres = capacidadMaxima - matriculados
    // Si vacantesDisponibles <= 0 → no se puede matricular
    private int vacantesDisponibles;
}
