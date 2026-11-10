package com.developerander.microservice.colegio.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class EstudianteNoAptoException extends RuntimeException {

    public EstudianteNoAptoException(String message) {
        super(message);
    }
}
