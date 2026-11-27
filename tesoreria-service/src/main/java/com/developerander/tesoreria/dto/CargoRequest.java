package com.developerander.tesoreria.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

// DTO de entrada para POST /api/v1/cargos
@Data
public class CargoRequest {

    @NotBlank(message = "El idEstudiante es obligatorio")
    private String idEstudiante;

    @NotBlank(message = "El concepto es obligatorio")
    private String concepto;

    @NotNull(message = "El monto es obligatorio")
    private BigDecimal monto;

    @NotNull(message = "La fecha de vencimiento es obligatoria")
    private LocalDate fechaVencimiento;
}
