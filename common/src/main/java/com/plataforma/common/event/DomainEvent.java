package com.plataforma.common.event;

import java.time.Instant;
import java.util.Map;

/**
 * Evento de dominio que viaja por Kafka entre microservicios.
 *
 * @param eventType  tipo de evento, por ejemplo {@code incidencia.creada}
 * @param occurredAt instante en que ocurrió
 * @param payload    datos del evento (valores simples serializables a JSON)
 */
public record DomainEvent(String eventType, Instant occurredAt, Map<String, Object> payload) {

    public static DomainEvent of(String eventType, Map<String, Object> payload) {
        return new DomainEvent(eventType, Instant.now(), payload);
    }
}
