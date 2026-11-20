package com.developerander.microservice.colegio.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

// ══════════════════════════════════════════════════════════════════════
// PUBLICADOR DE EVENTOS: MatriculaEventPublisher
//
// Responsabilidad única: publicar eventos a RabbitMQ.
// No sabe qué hace el consumidor (NotificacionService), solo publica.
//
// Este es el patrón "Fire and Forget" (asíncrono):
//   - El publisher NO espera respuesta del NotificacionService
//   - MatriculaService termina su trabajo sin bloquearse
//   - NotificacionService consume el mensaje cuando puede
//
// @Component = Spring registra esta clase como bean inyectable
// ══════════════════════════════════════════════════════════════════════
@Component
public class MatriculaEventPublisher {

    // Logger para registrar en consola qué ocurre con los eventos
    private static final Logger log = LoggerFactory.getLogger(MatriculaEventPublisher.class);

    // RabbitTemplate = herramienta de Spring para enviar mensajes a RabbitMQ
    @Autowired
    private RabbitTemplate rabbitTemplate;

    // Nombre del exchange leído desde application.properties
    // Exchange = punto de entrada de RabbitMQ que enruta mensajes a colas
    @Value("${rabbitmq.exchange.matriculas}")
    private String exchange;

    // Routing key = "etiqueta" que RabbitMQ usa para saber a qué cola enviar el mensaje
    @Value("${rabbitmq.routingkey.matricula-registrada}")
    private String routingKey;

    // ── MÉTODO PRINCIPAL: publicar evento ─────────────────────────────
    public void publicarMatriculaRegistrada(MatriculaRegistradaEvent evento) {
        try {
            // Agrega el timestamp justo antes de publicar
            evento.setTimestamp(LocalDateTime.now().toString());

            // convertAndSend:
            //   - Serializa el objeto Java a JSON automáticamente (Jackson)
            //   - Lo envía al exchange con la routing key especificada
            //   - RabbitMQ lo entrega a la cola "matricula.registrada"
            rabbitTemplate.convertAndSend(exchange, routingKey, evento);

            log.info("✅ [RabbitMQ] Evento publicado → Exchange: {} | RoutingKey: {} | IdMatricula: {}",
                    exchange, routingKey, evento.getIdMatricula());

        } catch (AmqpException e) {
            // Si RabbitMQ no está disponible (ej: en desarrollo sin RabbitMQ),
            // logueamos el error pero NO rompemos el flujo principal.
            // La matrícula ya se guardó en MySQL; el correo se enviará cuando RabbitMQ esté.
            log.warn("⚠️ [RabbitMQ] No se pudo publicar el evento (RabbitMQ no disponible): {}", e.getMessage());
            log.warn("⚠️ La matrícula {} se registró correctamente en BD, pero la notificación no se envió.",
                    evento.getIdMatricula());
        }
    }
}
