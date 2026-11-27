package com.developerander.microservice.colegio.exception;

import lombok.Getter;

@Getter
public class ReglaNegocioException extends RuntimeException {

    private final String codigo;
    private final int httpStatus;

    public ReglaNegocioException(String codigo, String mensaje, int httpStatus) {
        super(mensaje);
        this.codigo = codigo;
        this.httpStatus = httpStatus;
    }
}
