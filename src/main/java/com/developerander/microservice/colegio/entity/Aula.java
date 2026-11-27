package com.developerander.microservice.colegio.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Data
@Entity
@Table(name = "aulas")
public class Aula {

    @Id
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "id_aula", columnDefinition = "CHAR(36)")
    private UUID idAula;

    @Column(nullable = false, length = 10)
    private String periodo;

    @Column(nullable = false, length = 30)
    private String grado;

    @Column(nullable = false, length = 10)
    private String seccion;

    @Column(name = "capacidad_maxima", nullable = false)
    private int capacidadMaxima;

    @Column(nullable = false)
    private int vacantes;
}
