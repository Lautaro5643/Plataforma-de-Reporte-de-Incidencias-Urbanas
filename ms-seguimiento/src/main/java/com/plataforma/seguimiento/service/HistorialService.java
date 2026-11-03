package com.plataforma.seguimiento.service;

import com.plataforma.common.event.DomainEvent;
import com.plataforma.common.event.EventPublisher;
import com.plataforma.common.exception.ResourceNotFoundException;
import com.plataforma.seguimiento.dto.HistorialRequestDTO;
import com.plataforma.seguimiento.dto.HistorialResponseDTO;
import com.plataforma.seguimiento.entity.HistorialSeguimiento;
import com.plataforma.seguimiento.mapper.HistorialMapper;
import com.plataforma.seguimiento.repository.HistorialRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * El historial es un registro de auditoría: solo se crean y consultan entradas,
 * nunca se modifican ni se eliminan.
 */
@Service
public class HistorialService {

    public static final String TOPIC = "seguimiento-eventos";

    private final HistorialRepository repository;
    private final HistorialMapper mapper;
    private final EventPublisher publisher;

    public HistorialService(HistorialRepository repository, HistorialMapper mapper, EventPublisher publisher) {
        this.repository = repository;
        this.mapper = mapper;
        this.publisher = publisher;
    }

    @Transactional
    public HistorialResponseDTO registrar(HistorialRequestDTO dto) {
        HistorialSeguimiento guardado = repository.save(mapper.toEntity(dto));
        Map<String, Object> payload = new HashMap<>();
        payload.put("historialId", guardado.getId());
        payload.put("reporteId", guardado.getIdReporte());
        payload.put("estadoNuevo", guardado.getEstadoNuevo());
        publisher.publish(TOPIC, String.valueOf(guardado.getIdReporte()),
                DomainEvent.of("seguimiento.registrado", payload));
        return mapper.toResponse(guardado);
    }

    @Transactional(readOnly = true)
    public HistorialResponseDTO obtenerPorId(Long id) {
        return repository.findById(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Historial de seguimiento", id));
    }

    @Transactional(readOnly = true)
    public List<HistorialResponseDTO> listar() {
        return repository.findAllByOrderByFechaCambioDesc().stream().map(mapper::toResponse).toList();
    }

    /** Historial de un reporte, del cambio más reciente al más antiguo. */
    @Transactional(readOnly = true)
    public List<HistorialResponseDTO> listarPorReporte(Long idReporte) {
        return repository.findByIdReporteOrderByFechaCambioDesc(idReporte).stream()
                .map(mapper::toResponse)
                .toList();
    }
}
