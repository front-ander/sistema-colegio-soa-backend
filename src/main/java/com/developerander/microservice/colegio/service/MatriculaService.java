package com.developerander.microservice.colegio.service;

import com.developerander.microservice.colegio.entity.MatriEntity;

import java.util.List;

public interface MatriculaService {

    MatriEntity registrarMatricula(MatriEntity matriEntity);

    List<MatriEntity> listarMatriculas();
}
