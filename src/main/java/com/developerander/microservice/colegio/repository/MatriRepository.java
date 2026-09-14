package com.developerander.microservice.colegio.repository;

import com.developerander.microservice.colegio.entity.MatriEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MatriRepository extends JpaRepository<MatriEntity, Long> {

}
