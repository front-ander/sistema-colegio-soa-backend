package com.developerander.microservice.colegio.repository;

import com.developerander.microservice.colegio.entity.MatriEntity;
import com.developerander.microservice.colegio.entity.EstadoMatricula;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

// ══════════════════════════════════════════════════════════════════════
// REPOSITORY: MatriRepository
// Extiende JpaRepository<ENTIDAD, TIPO_DEL_ID>
//   - ENTIDAD    = MatriEntity  (nuestra tabla)
//   - TIPO_DEL_ID = UUID        (tipo de la clave primaria idMatricula)
//
// JpaRepository ya nos da GRATIS:
//   - findAll()          → SELECT * FROM matricula
//   - findById(id)       → SELECT * WHERE id_matricula = ?
//   - save(entity)       → INSERT o UPDATE automático
//   - deleteById(id)     → DELETE WHERE id_matricula = ?
//   - count()            → COUNT(*)
//   - existsById(id)     → si existe el registro
// ══════════════════════════════════════════════════════════════════════
@Repository
public interface MatriRepository extends JpaRepository<MatriEntity, UUID> {

    // ── CONSULTAS PERSONALIZADAS (Spring Data las genera automáticamente) ──

    // Busca todas las matrículas filtrando por estado (REGISTRADA o ANULADA)
    // Spring Data JPA convierte el nombre del método en SQL:
    // → SELECT * FROM matricula WHERE estado = ?
    List<MatriEntity> findByEstado(EstadoMatricula estado);

    // Busca todas las matrículas de un estudiante específico por su UUID
    // → SELECT * FROM matricula WHERE id_estudiante = ?
    List<MatriEntity> findByIdEstudiante(UUID idEstudiante);

}
