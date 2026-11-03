package com.plataforma.incidencias.service;

import com.plataforma.common.event.DomainEvent;
import com.plataforma.common.event.EventPublisher;
import com.plataforma.common.exception.BadRequestException;
import com.plataforma.common.exception.ResourceNotFoundException;
import com.plataforma.incidencias.dto.AsignarCuadrillaRequestDTO;
import com.plataforma.incidencias.dto.CambioEstadoRequestDTO;
import com.plataforma.incidencias.dto.ReporteRequestDTO;
import com.plataforma.incidencias.dto.ReporteResponseDTO;
import com.plataforma.incidencias.entity.EstadoReporte;
import com.plataforma.incidencias.entity.Reporte;
import com.plataforma.incidencias.mapper.ReporteMapper;
import com.plataforma.incidencias.repository.ReporteRepository;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReporteService {

    public static final String TOPIC = "incidencias-eventos";

    public static final String EVENTO_CREADA = "incidencia.creada";
    public static final String EVENTO_ESTADO_CAMBIADO = "incidencia.estado-cambiado";
    public static final String EVENTO_CUADRILLA_ASIGNADA = "incidencia.cuadrilla-asignada";

    private final ReporteRepository repository;
    private final ReporteMapper mapper;
    private final EventPublisher publisher;

    public ReporteService(ReporteRepository repository, ReporteMapper mapper, EventPublisher publisher) {
        this.repository = repository;
        this.mapper = mapper;
        this.publisher = publisher;
    }

    @Transactional
    public ReporteResponseDTO crear(ReporteRequestDTO dto) {
        Reporte guardado = repository.save(mapper.toEntity(dto));
        Map<String, Object> payload = payloadBase(guardado);
        payload.put("idCiudadano", guardado.getIdCiudadano());
        payload.put("idZona", guardado.getIdZona());
        payload.put("tipoIncidencia", guardado.getTipoIncidencia().name());
        payload.put("prioridad", guardado.getPrioridad().name());
        payload.put("estado", guardado.getEstadoActual().name());
        publicar(EVENTO_CREADA, guardado, payload);
        return mapper.toResponse(guardado);
    }

    @Transactional(readOnly = true)
    public ReporteResponseDTO obtenerPorId(Long id) {
        return mapper.toResponse(buscar(id));
    }

    /** Lista reportes (más recientes primero), aplicando solo los filtros que no sean nulos. */
    @Transactional(readOnly = true)
    public List<ReporteResponseDTO> listar(EstadoReporte estado, Long idZona, Long idCiudadano) {
        Specification<Reporte> spec = (root, query, cb) -> cb.conjunction();
        if (estado != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("estadoActual"), estado));
        }
        if (idZona != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("idZona"), idZona));
        }
        if (idCiudadano != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("idCiudadano"), idCiudadano));
        }
        return repository.findAll(spec, Sort.by(Sort.Direction.DESC, "fechaCreacion")).stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Transactional
    public ReporteResponseDTO actualizar(Long id, ReporteRequestDTO dto) {
        Reporte existente = buscar(id);
        if (existente.getEstadoActual().esFinal()) {
            throw new BadRequestException("No se puede modificar un reporte en estado "
                    + existente.getEstadoActual());
        }
        mapper.updateEntity(existente, dto);
        return mapper.toResponse(repository.save(existente));
    }

    /** Cambia el estado validando la transición; si el nuevo estado es final registra la fecha de cierre. */
    @Transactional
    public ReporteResponseDTO cambiarEstado(Long id, CambioEstadoRequestDTO dto) {
        Reporte reporte = buscar(id);
        EstadoReporte anterior = reporte.getEstadoActual();
        EstadoReporte nuevo = dto.nuevoEstado();

        if (!anterior.puedePasarA(nuevo)) {
            throw new BadRequestException("Transición de estado inválida: " + anterior + " -> " + nuevo);
        }
        if (nuevo == EstadoReporte.ASIGNADO && reporte.getIdCuadrilla() == null) {
            throw new BadRequestException("Debe asignar una cuadrilla antes de pasar el reporte a ASIGNADO");
        }

        reporte.setEstadoActual(nuevo);
        if (nuevo.esFinal()) {
            reporte.setFechaCierre(LocalDateTime.now());
        }
        Reporte guardado = repository.save(reporte);
        publicarCambioEstado(guardado, anterior, dto.observaciones(), dto.usuarioResponsable());
        return mapper.toResponse(guardado);
    }

    /** Asigna una cuadrilla; si el reporte estaba CREADO pasa automáticamente a ASIGNADO. */
    @Transactional
    public ReporteResponseDTO asignarCuadrilla(Long id, AsignarCuadrillaRequestDTO dto) {
        Reporte reporte = buscar(id);
        EstadoReporte anterior = reporte.getEstadoActual();
        if (anterior != EstadoReporte.CREADO && anterior != EstadoReporte.ASIGNADO) {
            throw new BadRequestException(
                    "Solo se puede asignar una cuadrilla a reportes en estado CREADO o ASIGNADO");
        }

        reporte.setIdCuadrilla(dto.idCuadrilla());
        boolean cambiaEstado = anterior == EstadoReporte.CREADO;
        if (cambiaEstado) {
            reporte.setEstadoActual(EstadoReporte.ASIGNADO);
        }
        Reporte guardado = repository.save(reporte);

        Map<String, Object> payload = payloadBase(guardado);
        payload.put("idCuadrilla", guardado.getIdCuadrilla());
        publicar(EVENTO_CUADRILLA_ASIGNADA, guardado, payload);
        if (cambiaEstado) {
            publicarCambioEstado(guardado, anterior, "Cuadrilla asignada", dto.usuarioResponsable());
        }
        return mapper.toResponse(guardado);
    }

    @Transactional
    public void eliminar(Long id) {
        repository.delete(buscar(id));
    }

    private Reporte buscar(Long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Reporte", id));
    }

    private Map<String, Object> payloadBase(Reporte reporte) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("reporteId", reporte.getId());
        return payload;
    }

    private void publicarCambioEstado(Reporte reporte, EstadoReporte anterior, String observaciones,
                                      String usuarioResponsable) {
        Map<String, Object> payload = payloadBase(reporte);
        payload.put("estadoAnterior", anterior.name());
        payload.put("estadoNuevo", reporte.getEstadoActual().name());
        payload.put("observaciones", observaciones);
        payload.put("usuarioResponsable", usuarioResponsable);
        publicar(EVENTO_ESTADO_CAMBIADO, reporte, payload);
    }

    private void publicar(String tipo, Reporte reporte, Map<String, Object> payload) {
        publisher.publish(TOPIC, String.valueOf(reporte.getId()), DomainEvent.of(tipo, payload));
    }
}
