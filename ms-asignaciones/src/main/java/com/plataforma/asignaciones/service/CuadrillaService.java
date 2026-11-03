package com.plataforma.asignaciones.service;

import com.plataforma.asignaciones.dto.CuadrillaRequestDTO;
import com.plataforma.asignaciones.dto.CuadrillaResponseDTO;
import com.plataforma.asignaciones.entity.Cuadrilla;
import com.plataforma.asignaciones.entity.Especialidad;
import com.plataforma.asignaciones.entity.EstadoCuadrilla;
import com.plataforma.asignaciones.mapper.CuadrillaMapper;
import com.plataforma.asignaciones.repository.CuadrillaRepository;
import com.plataforma.common.event.DomainEvent;
import com.plataforma.common.event.EventPublisher;
import com.plataforma.common.exception.BadRequestException;
import com.plataforma.common.exception.ConflictException;
import com.plataforma.common.exception.ResourceNotFoundException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CuadrillaService {

    public static final String TOPIC = "asignaciones-eventos";

    /** Cantidad de reportes activos a partir de la cual una cuadrilla pasa a OCUPADA. */
    public static final int MAX_REPORTES_ACTIVOS = 5;

    private final CuadrillaRepository repository;
    private final CuadrillaMapper mapper;
    private final EventPublisher publisher;

    public CuadrillaService(CuadrillaRepository repository, CuadrillaMapper mapper, EventPublisher publisher) {
        this.repository = repository;
        this.mapper = mapper;
        this.publisher = publisher;
    }

    @Transactional
    public CuadrillaResponseDTO crear(CuadrillaRequestDTO dto) {
        if (repository.existsByNombre(dto.nombre())) {
            throw new ConflictException("Ya existe una cuadrilla con el nombre " + dto.nombre());
        }
        Cuadrilla guardada = repository.save(mapper.toEntity(dto));
        publicar("cuadrilla.creada", guardada);
        return mapper.toResponse(guardada);
    }

    @Transactional(readOnly = true)
    public CuadrillaResponseDTO obtenerPorId(Long id) {
        return mapper.toResponse(buscar(id));
    }

    /** Lista cuadrillas, opcionalmente filtradas por zona y/o estado. */
    @Transactional(readOnly = true)
    public List<CuadrillaResponseDTO> listar(Long idZona, EstadoCuadrilla estado) {
        List<Cuadrilla> cuadrillas;
        if (idZona != null && estado != null) {
            cuadrillas = repository.findByIdZonaAndEstado(idZona, estado);
        } else if (idZona != null) {
            cuadrillas = repository.findByIdZona(idZona);
        } else if (estado != null) {
            cuadrillas = repository.findByEstado(estado);
        } else {
            cuadrillas = repository.findAll();
        }
        return cuadrillas.stream().map(mapper::toResponse).toList();
    }

    @Transactional
    public CuadrillaResponseDTO actualizar(Long id, CuadrillaRequestDTO dto) {
        Cuadrilla existente = buscar(id);
        if (repository.existsByNombreAndIdNot(dto.nombre(), id)) {
            throw new ConflictException("Ya existe otra cuadrilla con el nombre " + dto.nombre());
        }
        mapper.updateEntity(existente, dto);
        Cuadrilla guardada = repository.save(existente);
        publicar("cuadrilla.actualizada", guardada);
        return mapper.toResponse(guardada);
    }

    @Transactional
    public void eliminar(Long id) {
        Cuadrilla existente = buscar(id);
        repository.delete(existente);
        publicar("cuadrilla.eliminada", existente);
    }

    /**
     * Motor de asignación: entre las cuadrillas DISPONIBLES de la zona, elige la que tenga la especialidad
     * pedida (o GENERAL como alternativa). Prefiere la especialidad exacta y, en empate, la de menos carga.
     */
    @Transactional(readOnly = true)
    public CuadrillaResponseDTO sugerirCuadrilla(Long idZona, Especialidad especialidad) {
        return repository.findByIdZonaAndEstado(idZona, EstadoCuadrilla.DISPONIBLE).stream()
                .filter(c -> c.getEspecialidad() == especialidad || c.getEspecialidad() == Especialidad.GENERAL)
                .min(Comparator
                        .comparingInt((Cuadrilla c) -> c.getEspecialidad() == especialidad ? 0 : 1)
                        .thenComparingInt(Cuadrilla::getReportesActivos))
                .map(mapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("No hay cuadrillas disponibles en la zona "
                        + idZona + " para la especialidad " + especialidad));
    }

    /** Registra un reporte más a cargo de la cuadrilla; al llegar al máximo pasa a OCUPADA. */
    @Transactional
    public CuadrillaResponseDTO asignarReporte(Long id) {
        Cuadrilla cuadrilla = buscar(id);
        if (cuadrilla.getEstado() != EstadoCuadrilla.DISPONIBLE) {
            throw new BadRequestException("La cuadrilla " + id + " no está disponible (estado: "
                    + cuadrilla.getEstado() + ")");
        }
        cuadrilla.setReportesActivos(cuadrilla.getReportesActivos() + 1);
        if (cuadrilla.getReportesActivos() >= MAX_REPORTES_ACTIVOS) {
            cuadrilla.setEstado(EstadoCuadrilla.OCUPADA);
        }
        Cuadrilla guardada = repository.save(cuadrilla);
        publicar("cuadrilla.reporte-asignado", guardada);
        return mapper.toResponse(guardada);
    }

    /** Libera un reporte de la cuadrilla; si estaba OCUPADA y baja del máximo vuelve a DISPONIBLE. */
    @Transactional
    public CuadrillaResponseDTO liberarReporte(Long id) {
        Cuadrilla cuadrilla = buscar(id);
        if (cuadrilla.getReportesActivos() <= 0) {
            throw new BadRequestException("La cuadrilla " + id + " no tiene reportes activos para liberar");
        }
        cuadrilla.setReportesActivos(cuadrilla.getReportesActivos() - 1);
        if (cuadrilla.getEstado() == EstadoCuadrilla.OCUPADA
                && cuadrilla.getReportesActivos() < MAX_REPORTES_ACTIVOS) {
            cuadrilla.setEstado(EstadoCuadrilla.DISPONIBLE);
        }
        Cuadrilla guardada = repository.save(cuadrilla);
        publicar("cuadrilla.reporte-liberado", guardada);
        return mapper.toResponse(guardada);
    }

    private Cuadrilla buscar(Long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Cuadrilla", id));
    }

    private void publicar(String tipo, Cuadrilla cuadrilla) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("cuadrillaId", cuadrilla.getId());
        payload.put("nombre", cuadrilla.getNombre());
        payload.put("idZona", cuadrilla.getIdZona());
        payload.put("estado", cuadrilla.getEstado().name());
        payload.put("reportesActivos", cuadrilla.getReportesActivos());
        publisher.publish(TOPIC, String.valueOf(cuadrilla.getId()), DomainEvent.of(tipo, payload));
    }
}
