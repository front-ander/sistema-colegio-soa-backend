package com.developerander.microservice.colegio.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO ligero para recibir unicamente los datos necesarios de EstudianteService
 * respetando el bajo acoplamiento SOA.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class EstudianteResponseDTO {

    private Long id;
    private String dni;
    private String nombres;
    private String estado;
}
