package com.developerander.microservice.colegio.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;    // Aplica seguridad a todos los endpoints
import io.swagger.v3.oas.models.security.SecurityScheme;         // Define el esquema de autenticación
import io.swagger.v3.oas.models.Components;                      // Contiene los schemas reutilizables
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// ══════════════════════════════════════════════════════════════════════
// CONFIGURACIÓN SWAGGER — Versión SOA con autenticación JWT
//
// Cambio clave respecto a la versión anterior:
//   - Agrega el botón "🔒 Authorize" en Swagger UI
//   - Permite ingresar el Bearer token para probar endpoints protegidos
//   - Documenta que la API requiere autenticación (como el ESB WSO2)
//
// Cuando jwt.enabled=false (desarrollo), el token no es validado
// pero el botón Authorize sigue disponible para demostrar el flujo SOA
// ══════════════════════════════════════════════════════════════════════
@Configuration
public class SwaggerConfig {

    // Nombre del esquema de seguridad (referenciado en SecurityRequirement)
    private static final String SECURITY_SCHEME_NAME = "BearerAuth";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                // ── Información general de la API ─────────────────────
                .info(new Info()
                        .title("🎓 Microservicio de Matrículas — SOA | APF3")
                        .version("1.0.0")
                        .description("""
                                **API REST del servicio de matrículas escolares con arquitectura SOA.**
                                
                                ### 🏛️ Arquitectura implementada:
                                - **ESB Simulado**: Filtro X-Correlation-ID + validación JWT
                                - **Orquestador BPM**: ProcesoMatricula (simula Flowable)
                                - **Clientes SOA**: TesoreriaClient + AulaClient
                                - **Mensajería async**: RabbitMQ → NotificacionService
                                
                                ### 🔐 Autenticación:
                                En desarrollo: `jwt.enabled=false` → no requiere token.
                                En producción: WSO2 MI valida el Bearer JWT antes de enrutar.
                                
                                ### 📋 Proceso de matrícula (BPM):
                                1. Verificar deudas → TesoreriaService
                                2. Verificar vacantes → AulaService  
                                3. Registrar en MySQL
                                4. Publicar evento → RabbitMQ → NotificacionService
                                
                                ### 🔗 Endpoints versionados:
                                Todas las rutas siguen el estándar APF2 Tabla 14: `/api/v1/`
                                """)
                        .contact(new Contact()
                                .name("DeveloperAnder")
                                .email("developerander@example.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT"))
                )

                // ── Componente de seguridad: define el esquema JWT ────
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        // HTTP = el token va en el header Authorization
                                        .type(SecurityScheme.Type.HTTP)
                                        // "bearer" = formato "Bearer <token>"
                                        .scheme("bearer")
                                        // bearerFormat = hint para Swagger UI (solo informativo)
                                        .bearerFormat("JWT")
                                        .description("Token JWT generado por el AuthService. " +
                                                     "Formato: Bearer <token>")
                        )
                )

                // ── Aplica el esquema de seguridad a TODOS los endpoints ──
                // Esto hace que el candado 🔒 aparezca en cada endpoint de Swagger UI
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME));
    }
}
