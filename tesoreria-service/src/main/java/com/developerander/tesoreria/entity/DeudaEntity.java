package com.developerander.tesoreria.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

// Mapea la tabla `deudas` en la base de datos `colegio_tesoreria_db`
@Data
@Entity
@Table(name = "deudas")
public class DeudaEntity {

    @Id
    @Column(name = "id_deuda", length = 36)
    private String idDeuda;             // UUID en formato CHAR(36)

    @Column(name = "id_estudiante", nullable = false, length = 36)
    private String idEstudiante;        // Referencia lógica al estudiante

    @Column(nullable = false, length = 120)
    private String concepto;            // Ej: "Pensión Noviembre 2025 (Vencida)"

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal monto;           // Monto en soles con 2 decimales

    @Column(name = "fecha_vencimiento", nullable = false)
    private LocalDate fechaVencimiento; // Fecha límite de pago

    @Column(name = "deuda_vencida", nullable = false)
    private boolean deudaVencida;       // true = bloquea matrícula según Regla RN-01

    @Column(nullable = false)
    private boolean pagada;             // false = aún no ha sido pagada
}
