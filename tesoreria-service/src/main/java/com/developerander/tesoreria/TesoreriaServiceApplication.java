package com.developerander.tesoreria;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Punto de entrada principal del microservicio TesoreriaService (Puerto 8081)
@SpringBootApplication
public class TesoreriaServiceApplication {
    public static void main(String[] args) {
        // Inicia el servidor Tomcat embebido y registra todos los beans, filtros y controladores
        SpringApplication.run(TesoreriaServiceApplication.class, args);
    }
}
