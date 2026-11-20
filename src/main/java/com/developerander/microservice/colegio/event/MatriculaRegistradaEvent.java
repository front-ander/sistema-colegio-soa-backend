package com.developerander.microservice.colegio.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

// ══════════════════════════════════════════════════════════════════════
// EVENTO: MatriculaRegistradaEvent
//
// Mensaje que se publica a RabbitMQ después de registrar una matrícula.
// Este es el "contrato" del evento asíncrono SOA.
//
// Flujo asíncrono SOA:
//   MatriculaService → RabbitMQ (cola: matricula.registrada)
//                    → NotificacionService (suscriptor)
//                    → Envía email/SMS al estudiante
//
// Por qué asíncrono: el cliente recibe su 201 inmediatamente,
// sin esperar que el correo se envíe (puede tardar segundos).
// ══════════════════════════════════════════════════════════════════════
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MatriculaRegistradaEvent {

    // ID de la matrícula recién creada (para que NotificacionService pueda referenciarlo)
    private UUID idMatricula;

    // ID del estudiante que recibirá la notificación
    private UUID idEstudiante;

    // Fecha en que se registró la matrícula
    private LocalDate fechaMatricula;

    // Estado final (siempre REGISTRADA al momento del evento)
    private String estado;

    // ID de correlación del ESB (para rastrear la petición en los logs)
    private String correlationId;

    // Timestamp del evento (cuándo se publicó)
    private String timestamp;
}
