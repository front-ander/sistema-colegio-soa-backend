package com.developerander.microservice.colegio.entity;

import jakarta.persistence.*;           // Anotaciones JPA: @Entity, @Table, @Id, @Column, etc.
import jakarta.validation.constraints.NotNull; // Validación: campo no puede ser null al recibir JSON
import lombok.Data;                     // Lombok: genera getters, setters, equals, hashCode, toString
import java.time.LocalDate;             // Tipo de fecha sin hora (ej: 2026-11-19)
import java.util.UUID;                  // Identificador único universal (más seguro que Long)

// @Data = Lombok genera automáticamente todos los getters/setters/toString/equals
@Data

// @Entity = Le dice a Hibernate que esta clase es una tabla en la base de datos
@Entity

// @Table = Especifica el nombre exacto de la tabla en MySQL
@Table(name = "matricula")

public class MatriEntity {

    // ── CLAVE PRIMARIA ─────────────────────────────────────────────────
    @Id
    // @GeneratedValue con UUID = Hibernate genera automáticamente un UUID único
    // No usamos IDENTITY porque UUID no es numérico como AUTO_INCREMENT
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_matricula")          // Nombre de la columna en MySQL
    private UUID idMatricula;

    // ── RELACIONES CON OTROS MICROSERVICIOS (IDs externos) ────────────
    // En arquitectura de microservicios, NO usamos @ManyToOne/@JoinColumn
    // porque cada servicio tiene su propia base de datos.
    // Solo guardamos los UUIDs de referencia.

    @NotNull(message = "El ID del estudiante es obligatorio")
    @Column(name = "id_estudiante", nullable = false)
    private UUID idEstudiante;              // Referencia al microservicio de Estudiantes

    @NotNull(message = "El ID del cargo es obligatorio")
    @Column(name = "id_cargo", nullable = false)
    private UUID idCargo;                   // Referencia al microservicio de Cargos/Pagos

    @NotNull(message = "El ID del aula es obligatorio")
    @Column(name = "id_aula", nullable = false)
    private UUID idAula;                    // Referencia al microservicio de Aulas

    // ── DATOS DE LA MATRÍCULA ─────────────────────────────────────────
    @NotNull(message = "La fecha de matrícula es obligatoria")
    @Column(name = "fecha_matricula", nullable = false)
    private LocalDate fechaMatricula;       // Fecha en que se realizó la matrícula

    // @Enumerated(EnumType.STRING) = Guarda "REGISTRADA" o "ANULADA" en MySQL
    // Si usáramos EnumType.ORDINAL guardaría 0 o 1 (menos legible en la BD)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoMatricula estado;         // Estado: REGISTRADA o ANULADA (lo pone el Service)

    // Campo transitorio (no se guarda en BD) para probar compensación Saga en desarrollo
    @Transient
    private boolean simularFallo = false;

}
