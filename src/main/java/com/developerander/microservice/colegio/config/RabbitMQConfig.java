package com.developerander.microservice.colegio.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// ══════════════════════════════════════════════════════════════════════
// CONFIGURACIÓN: RabbitMQConfig
//
// Define la infraestructura de mensajería asíncrona SOA:
//   - Exchange: punto de entrada (router de mensajes)
//   - Queue: cola donde se acumulan los mensajes
//   - Binding: regla que conecta exchange con cola vía routing key
//
// Analogía:
//   Exchange = oficina de correos
//   Routing Key = código postal
//   Queue = buzón de correos del destinatario
//   Mensaje = carta
//
// RabbitMQ crea estas estructuras automáticamente al arrancar si no existen.
// ══════════════════════════════════════════════════════════════════════
@Configuration
public class RabbitMQConfig {

    // ── CONSTANTES: nombres de exchange, cola y routing key ───────────
    // Las constantes evitan typos al referenciarlas en otros archivos

    // Exchange de tipo "direct": enruta mensajes por routing key exacta
    public static final String EXCHANGE_MATRICULAS = "exchange.matriculas";

    // Cola donde NotificacionService recibe los eventos de matrícula
    public static final String QUEUE_MATRICULA_REGISTRADA = "matricula.registrada";

    // La routing key conecta el exchange con la cola específica
    public static final String ROUTING_KEY_MATRICULA_REGISTRADA = "rk.matricula.registrada";

    // ── BEAN: Exchange ────────────────────────────────────────────────
    @Bean
    public DirectExchange matriculasExchange() {
        // DirectExchange = enruta el mensaje SOLO a la cola cuyo binding key coincide exactamente
        // durable=true: el exchange sobrevive a reinicios de RabbitMQ
        return new DirectExchange(EXCHANGE_MATRICULAS, true, false);
    }

    // ── BEAN: Cola ────────────────────────────────────────────────────
    @Bean
    public Queue matriculaRegistradaQueue() {
        // durable=true: los mensajes NO se pierden si RabbitMQ se reinicia
        // (se persisten en disco hasta que el consumidor los procese)
        return new Queue(QUEUE_MATRICULA_REGISTRADA, true);
    }

    // ── BEAN: Binding (conexión exchange → cola) ───────────────────────
    @Bean
    public Binding bindingMatriculaRegistrada(Queue matriculaRegistradaQueue,
                                               DirectExchange matriculasExchange) {
        // "Si llega al exchange EXCHANGE_MATRICULAS un mensaje con routing key
        //  ROUTING_KEY_MATRICULA_REGISTRADA, envíalo a la cola QUEUE_MATRICULA_REGISTRADA"
        return BindingBuilder
                .bind(matriculaRegistradaQueue)
                .to(matriculasExchange)
                .with(ROUTING_KEY_MATRICULA_REGISTRADA);
    }

    // ── BEAN: MessageConverter ────────────────────────────────────────
    @Bean
    public MessageConverter jsonMessageConverter() {
        // Serializa/deserializa los mensajes como JSON (en vez de bytes Java)
        // Permite que NotificacionService (escrito en otro lenguaje) lea el mensaje
        return new Jackson2JsonMessageConverter();
    }

    // ── BEAN: RabbitTemplate (configurado con JSON) ────────────────────
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        // Usa el convertidor JSON para que todos los mensajes salgan como JSON
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}
