package com.developerander.tesoreria.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

// Mapea la tabla `cargos` en la base de datos `colegio_tesoreria_db`
@Data
@Entity
@Table(name = "cargos")
public class CargoEntity {

    @Id
    @Column(name = "id_cargo", length = 36)
    private String idCargo;             // UUID generado al crear el cargo

    @Column(name = "id_estudiante", nullable = false, length = 36)
    private String idEstudiante;        // Estudiante al que se le genera el cargo

    @Column(nullable = false, length = 120)
    private String concepto;            // Ej: "Matrícula 2026"

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal monto;           // Importe del cargo (ej: 300.00)

    @Column(name = "fecha_vencimiento", nullable = false)
    private LocalDate fechaVencimiento; // Fecha límite de pago

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    private EstadoCargo estado = EstadoCargo.PENDIENTE; // Inicia como PENDIENTE

    @Column(name = "motivo_anulacion", length = 255)
    private String motivoAnulacion;     // Se llena cuando se ejecuta compensación Saga
}
