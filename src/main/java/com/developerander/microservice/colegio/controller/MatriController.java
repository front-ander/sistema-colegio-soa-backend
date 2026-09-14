package com.developerander.microservice.colegio.controller;

import com.developerander.microservice.colegio.entity.MatriEntity;
import com.developerander.microservice.colegio.repository.MatriRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/matriculs")
public class MatriController {

    @Autowired
    private MatriRepository matriRepository;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List <MatriEntity> getALLMatriculs() {
        return matriRepository.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED) // Devuelve un código 201 de "Creado"
    public void createMatricula(@RequestBody MatriEntity matriEntity) {
        matriRepository.save(matriEntity); // Esta es la magia que lo guarda en MySQL
    }
}


