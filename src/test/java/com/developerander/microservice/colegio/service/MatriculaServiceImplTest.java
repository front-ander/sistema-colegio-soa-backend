package com.developerander.microservice.colegio.service;

import com.developerander.microservice.colegio.dto.EstudianteResponseDTO;
import com.developerander.microservice.colegio.entity.MatriEntity;
import com.developerander.microservice.colegio.exception.EstudianteNoAptoException;
import com.developerander.microservice.colegio.repository.MatriRepository;
import com.developerander.microservice.colegio.service.impl.MatriculaServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MatriculaServiceImplTest {

    @Mock
    private MatriRepository matriRepository;

    @Mock
    private RestTemplate restTemplate;

    private MatriculaServiceImpl matriculaService;

    private final String baseUrl = "http://localhost:8082/api/v1/estudiantes/dni/";

    @BeforeEach
    void setUp() {
        matriculaService = new MatriculaServiceImpl(matriRepository, restTemplate, baseUrl);
    }

    @Test
    @DisplayName("Debe registrar matrícula exitosamente cuando el estudiante existe y está ACTIVO")
    void registrarMatricula_Exito() {
        String dni = "12345678";
        EstudianteResponseDTO mockEstudiante = EstudianteResponseDTO.builder()
                .id(1L)
                .dni(dni)
                .nombres("Ana Maria")
                .estado("ACTIVO")
                .build();

        when(restTemplate.getForEntity(eq(baseUrl + dni), eq(EstudianteResponseDTO.class)))
                .thenReturn(ResponseEntity.ok(mockEstudiante));

        MatriEntity input = MatriEntity.builder()
                .dni(dni)
                .pagoTotal(BigDecimal.valueOf(250.00))
                .build();

        MatriEntity saved = MatriEntity.builder()
                .id(10L)
                .estudianteId(1L)
                .dni(dni)
                .nombreAlumno("Ana Maria")
                .fechaMatricula(LocalDate.now())
                .pagoTotal(BigDecimal.valueOf(250.00))
                .build();

        when(matriRepository.save(any(MatriEntity.class))).thenReturn(saved);

        MatriEntity resultado = matriculaService.registrarMatricula(input);

        assertNotNull(resultado);
        assertEquals(10L, resultado.getId());
        assertEquals(1L, resultado.getEstudianteId());
        assertEquals(dni, resultado.getDni());
        assertEquals("Ana Maria", resultado.getNombreAlumno());
        verify(matriRepository, times(1)).save(any(MatriEntity.class));
    }

    @Test
    @DisplayName("Debe lanzar EstudianteNoAptoException cuando el estudiante no existe (404)")
    void registrarMatricula_EstudianteNoExiste_LanzaExcepcion() {
        String dni = "00000000";

        when(restTemplate.getForEntity(eq(baseUrl + dni), eq(EstudianteResponseDTO.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND, "Not Found"));

        MatriEntity input = MatriEntity.builder()
                .dni(dni)
                .pagoTotal(BigDecimal.valueOf(250.00))
                .build();

        EstudianteNoAptoException ex = assertThrows(EstudianteNoAptoException.class, () -> {
            matriculaService.registrarMatricula(input);
        });

        assertTrue(ex.getMessage().contains("no está apto para matricularse"));
        verify(matriRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar EstudianteNoAptoException cuando el estudiante está INACTIVO")
    void registrarMatricula_EstudianteInactivo_LanzaExcepcion() {
        String dni = "87654321";
        EstudianteResponseDTO mockEstudiante = EstudianteResponseDTO.builder()
                .id(2L)
                .dni(dni)
                .nombres("Juan Perez")
                .estado("INACTIVO")
                .build();

        when(restTemplate.getForEntity(eq(baseUrl + dni), eq(EstudianteResponseDTO.class)))
                .thenReturn(ResponseEntity.ok(mockEstudiante));

        MatriEntity input = MatriEntity.builder()
                .dni(dni)
                .build();

        EstudianteNoAptoException ex = assertThrows(EstudianteNoAptoException.class, () -> {
            matriculaService.registrarMatricula(input);
        });

        assertTrue(ex.getMessage().contains("no está apto para matricularse"));
        assertTrue(ex.getMessage().contains("INACTIVO"));
        verify(matriRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar EstudianteNoAptoException cuando no se proporciona DNI")
    void registrarMatricula_SinDni_LanzaExcepcion() {
        MatriEntity input = MatriEntity.builder()
                .pagoTotal(BigDecimal.valueOf(100.00))
                .build();

        assertThrows(EstudianteNoAptoException.class, () -> {
            matriculaService.registrarMatricula(input);
        });

        verify(restTemplate, never()).getForEntity(anyString(), any());
        verify(matriRepository, never()).save(any());
    }
}
