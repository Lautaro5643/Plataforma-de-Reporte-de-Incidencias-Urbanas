package com.plataforma.common.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Implementación de {@link EventPublisher} sobre Kafka.
 * Un fallo al publicar se registra en el log pero no interrumpe la operación de negocio.
 */
@Component
public class KafkaEventPublisher implements EventPublisher {

    private static final Logger log = LoggerFactory.getLogger(KafkaEventPublisher.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public KafkaEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publish(String topic, String key, DomainEvent event) {
        try {
            kafkaTemplate.send(topic, key, event).whenComplete((result, error) -> {
                if (error != null) {
                    log.warn("No se pudo publicar el evento {} en el topic {}: {}",
                            event.eventType(), topic, error.getMessage());
                } else {
                    log.debug("Evento {} publicado en el topic {}", event.eventType(), topic);
                }
            });
        } catch (Exception e) {
            log.warn("Error al publicar el evento {} en el topic {}: {}", event.eventType(), topic, e.getMessage());
        }
    }
}
