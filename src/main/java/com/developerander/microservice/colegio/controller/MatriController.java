package com.developerander.microservice.colegio.controller;

import com.developerander.microservice.colegio.entity.MatriEntity;
import com.developerander.microservice.colegio.service.MatriculaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/matriculas", "/api/matriculs"})
@RequiredArgsConstructor
public class MatriController {

    private final MatriculaService matriculaService;

    @GetMapping
    public ResponseEntity<List<MatriEntity>> getAllMatriculas() {
        return ResponseEntity.ok(matriculaService.listarMatriculas());
    }

    @PostMapping
    public ResponseEntity<MatriEntity> createMatricula(@RequestBody MatriEntity matriEntity) {
        MatriEntity nuevaMatricula = matriculaService.registrarMatricula(matriEntity);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevaMatricula);
    }
}
