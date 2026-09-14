package com.developerander.microservice.colegio.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Entity
@Table (name = "matriculas")
public class MatriEntity {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
        private String nombreAlumno; // Para textos
        private LocalDate fechaMatricula; // Para guardar fechas reales (ej: 2026-09-13)
        private BigDecimal pagoTotal; // Para decimales exactos. En Java, el dinero SIEMPRE se maneja con BigDecimal, no con Double.

}
