package com.developerander.microservice.colegio.entity;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "matriculas")
public class MatriEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonAlias({"estudianteId", "idEstudiante"})
    private Long estudianteId;

    @JsonAlias({"estudianteDni", "dni"})
    private String dni;

    private String nombreAlumno;

    private LocalDate fechaMatricula;

    private BigDecimal pagoTotal;
}
