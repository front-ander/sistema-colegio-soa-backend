package com.developerander.tesoreria.repository;

import com.developerander.tesoreria.entity.CargoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CargoRepository extends JpaRepository<CargoEntity, String> {
}
