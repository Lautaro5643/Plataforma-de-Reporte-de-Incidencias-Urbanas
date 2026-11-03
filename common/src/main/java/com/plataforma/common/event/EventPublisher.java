package com.plataforma.common.event;

/** Puerto de salida para publicar eventos de dominio (desacopla los servicios de Kafka). */
public interface EventPublisher {

    void publish(String topic, String key, DomainEvent event);
}
