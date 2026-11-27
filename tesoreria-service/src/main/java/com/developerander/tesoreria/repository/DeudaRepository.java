package com.developerander.tesoreria.repository;

import com.developerander.tesoreria.entity.DeudaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeudaRepository extends JpaRepository<DeudaEntity, String> {

    // Consulta derivada de Spring Data JPA:
    // Busca únicamente deudas VENCIDAS y NO PAGADAS del estudiante (Regla RN-01)
    List<DeudaEntity> findByIdEstudianteAndDeudaVencidaTrueAndPagadaFalse(String idEstudiante);
}
