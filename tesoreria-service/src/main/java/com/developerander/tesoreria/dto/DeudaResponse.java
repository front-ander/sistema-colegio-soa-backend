package com.developerander.tesoreria.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// DTO de respuesta para GET /api/v1/deudas/{idEstudiante}
// Mantiene exactamente el mismo contrato que espera TesoreriaClient en matricula-service
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeudaResponse {
    private boolean tieneDeuda;   // true si tiene >= 1 deuda vencida sin pagar (RN-01)
    private int cantidadDeudas;   // Número de cuotas vencidas
    private double montoTotal;    // Suma total adeudada en soles
    private String detalle;       // Mensaje informativo
}
