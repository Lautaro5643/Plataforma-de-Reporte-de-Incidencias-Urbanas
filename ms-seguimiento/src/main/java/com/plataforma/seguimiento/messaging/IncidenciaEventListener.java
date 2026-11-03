package com.plataforma.seguimiento.messaging;

import com.plataforma.common.event.DomainEvent;
import com.plataforma.seguimiento.dto.HistorialRequestDTO;
import com.plataforma.seguimiento.service.HistorialService;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/** Escucha los eventos de ms-incidencias y registra cada cambio en el historial de seguimiento. */
@Component
public class IncidenciaEventListener {

    public static final String TOPIC_INCIDENCIAS = "incidencias-eventos";
    public static final String EVENTO_CREADA = "incidencia.creada";
    public static final String EVENTO_ESTADO_CAMBIADO = "incidencia.estado-cambiado";
    public static final String USUARIO_SISTEMA = "sistema";

    private static final Logger log = LoggerFactory.getLogger(IncidenciaEventListener.class);

    private final HistorialService historialService;

    public IncidenciaEventListener(HistorialService historialService) {
        this.historialService = historialService;
    }

    @KafkaListener(topics = TOPIC_INCIDENCIAS, groupId = "ms-seguimiento")
    public void onIncidenciaEvent(DomainEvent event) {
        if (event == null || event.eventType() == null || event.payload() == null) {
            log.warn("Evento de incidencia ignorado: formato inválido");
            return;
        }
        Map<String, Object> payload = event.payload();
        Long reporteId = toLong(payload.get("reporteId"));
        if (reporteId == null) {
            log.warn("Evento {} ignorado: no incluye reporteId", event.eventType());
            return;
        }

        switch (event.eventType()) {
            case EVENTO_CREADA -> historialService.registrar(new HistorialRequestDTO(
                    reporteId,
                    null,
                    toText(payload.get("estado")),
                    "Reporte creado",
                    USUARIO_SISTEMA,
                    null));
            case EVENTO_ESTADO_CAMBIADO -> historialService.registrar(new HistorialRequestDTO(
                    reporteId,
                    toText(payload.get("estadoAnterior")),
                    toText(payload.get("estadoNuevo")),
                    toText(payload.get("observaciones")),
                    toText(payload.get("usuarioResponsable")) != null
                            ? toText(payload.get("usuarioResponsable"))
                            : USUARIO_SISTEMA,
                    null));
            default -> log.debug("Evento {} sin acción en seguimiento", event.eventType());
        }
    }

    private Long toLong(Object value) {
        return value instanceof Number number ? number.longValue() : null;
    }

    private String toText(Object value) {
        return value == null ? null : value.toString();
    }
}
