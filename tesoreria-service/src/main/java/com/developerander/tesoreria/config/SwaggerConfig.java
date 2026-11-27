package com.developerander.tesoreria.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI tesoreriaOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("💰 TesoreriaService — SOA | I.E.P. Señor de los Milagros")
                        .version("1.0.0")
                        .description("Microservicio financiero: consulta de deudas (RN-01), generación de cargos y anulación por compensación Saga."))
                .components(new Components()
                        .addSecuritySchemes("BearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList("BearerAuth"));
    }
}
