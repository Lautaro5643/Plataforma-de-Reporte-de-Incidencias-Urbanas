package com.plataforma.zonas.service;

import com.plataforma.common.event.DomainEvent;
import com.plataforma.common.event.EventPublisher;
import com.plataforma.common.exception.ConflictException;
import com.plataforma.common.exception.ResourceNotFoundException;
import com.plataforma.zonas.dto.ZonaRequestDTO;
import com.plataforma.zonas.dto.ZonaResponseDTO;
import com.plataforma.zonas.entity.Zona;
import com.plataforma.zonas.mapper.ZonaMapper;
import com.plataforma.zonas.repository.ZonaRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ZonaService {

    public static final String TOPIC = "zonas-eventos";

    private final ZonaRepository repository;
    private final ZonaMapper mapper;
    private final EventPublisher publisher;

    public ZonaService(ZonaRepository repository, ZonaMapper mapper, EventPublisher publisher) {
        this.repository = repository;
        this.mapper = mapper;
        this.publisher = publisher;
    }

    @Transactional
    public ZonaResponseDTO crear(ZonaRequestDTO dto) {
        if (repository.existsByNombre(dto.nombre())) {
            throw new ConflictException("Ya existe una zona con el nombre " + dto.nombre());
        }
        Zona guardada = repository.save(mapper.toEntity(dto));
        publicar("zona.creada", guardada);
        return mapper.toResponse(guardada);
    }

    @Transactional(readOnly = true)
    public ZonaResponseDTO obtenerPorId(Long id) {
        return mapper.toResponse(buscar(id));
    }

    /** Lista todas las zonas o, si se indica un código postal, solo las que le corresponden. */
    @Transactional(readOnly = true)
    public List<ZonaResponseDTO> listar(String codigoPostal) {
        List<Zona> zonas = (codigoPostal == null || codigoPostal.isBlank())
                ? repository.findAll()
                : repository.findByCodigoPostal(codigoPostal);
        return zonas.stream().map(mapper::toResponse).toList();
    }

    @Transactional
    public ZonaResponseDTO actualizar(Long id, ZonaRequestDTO dto) {
        Zona existente = buscar(id);
        if (repository.existsByNombreAndIdNot(dto.nombre(), id)) {
            throw new ConflictException("Ya existe otra zona con el nombre " + dto.nombre());
        }
        mapper.updateEntity(existente, dto);
        Zona guardada = repository.save(existente);
        publicar("zona.actualizada", guardada);
        return mapper.toResponse(guardada);
    }

    @Transactional
    public void eliminar(Long id) {
        Zona existente = buscar(id);
        repository.delete(existente);
        publicar("zona.eliminada", existente);
    }

    private Zona buscar(Long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Zona", id));
    }

    private void publicar(String tipo, Zona zona) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("zonaId", zona.getId());
        payload.put("nombre", zona.getNombre());
        payload.put("codigoPostal", zona.getCodigoPostal());
        publisher.publish(TOPIC, String.valueOf(zona.getId()), DomainEvent.of(tipo, payload));
    }
}
