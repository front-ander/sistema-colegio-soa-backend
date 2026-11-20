package com.developerander.microservice.colegio.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

// ══════════════════════════════════════════════════════════════════════
// CONFIGURACIÓN: RestTemplateConfig
//
// Registra un RestTemplate como bean de Spring.
//
// RestTemplate = cliente HTTP que permite llamar a otros microservicios.
// Se inyecta en TesoreriaClient y AulaClient con @Autowired.
//
// ¿Por qué como @Bean y no "new RestTemplate()"?
//   - Al ser un bean de Spring, podemos interceptarlo con:
//     - Logging interceptors
//     - Timeout configurado centralmente
//     - Circuit Breaker (Resilience4j) en el futuro
//   - Se crea UNA SOLA instancia (singleton) compartida por todos los clientes
// ══════════════════════════════════════════════════════════════════════
@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        // En el futuro se puede configurar timeouts aquí:
        // HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        // factory.setConnectTimeout(3000);  // 3 segundos máximo para conectar
        // factory.setReadTimeout(5000);     // 5 segundos máximo para leer respuesta
        // return new RestTemplate(factory);
        return new RestTemplate();
    }
}
