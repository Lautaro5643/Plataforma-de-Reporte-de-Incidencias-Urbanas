package com.plataforma.ciudadanos.service;

import com.plataforma.ciudadanos.dto.CiudadanoRequestDTO;
import com.plataforma.ciudadanos.dto.CiudadanoResponseDTO;
import com.plataforma.ciudadanos.entity.Ciudadano;
import com.plataforma.ciudadanos.mapper.CiudadanoMapper;
import com.plataforma.ciudadanos.repository.CiudadanoRepository;
import com.plataforma.common.event.DomainEvent;
import com.plataforma.common.event.EventPublisher;
import com.plataforma.common.exception.ConflictException;
import com.plataforma.common.exception.ResourceNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CiudadanoService {

    public static final String TOPIC = "ciudadanos-eventos";

    private final CiudadanoRepository repository;
    private final CiudadanoMapper mapper;
    private final EventPublisher publisher;

    public CiudadanoService(CiudadanoRepository repository, CiudadanoMapper mapper, EventPublisher publisher) {
        this.repository = repository;
        this.mapper = mapper;
        this.publisher = publisher;
    }

    @Transactional
    public CiudadanoResponseDTO crear(CiudadanoRequestDTO dto) {
        if (repository.existsByEmail(dto.email())) {
            throw new ConflictException("Ya existe un ciudadano con el email " + dto.email());
        }
        if (repository.existsByDni(dto.dni())) {
            throw new ConflictException("Ya existe un ciudadano con el DNI " + dto.dni());
        }
        Ciudadano guardado = repository.save(mapper.toEntity(dto));
        publicar("ciudadano.creado", guardado);
        return mapper.toResponse(guardado);
    }

    @Transactional(readOnly = true)
    public CiudadanoResponseDTO obtenerPorId(Long id) {
        return mapper.toResponse(buscar(id));
    }

    @Transactional(readOnly = true)
    public CiudadanoResponseDTO obtenerPorDni(String dni) {
        return repository.findByDni(dni)
                .map(mapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("No existe un ciudadano con el DNI " + dni));
    }

    @Transactional(readOnly = true)
    public List<CiudadanoResponseDTO> listar() {
        return repository.findAll().stream().map(mapper::toResponse).toList();
    }

    @Transactional
    public CiudadanoResponseDTO actualizar(Long id, CiudadanoRequestDTO dto) {
        Ciudadano existente = buscar(id);
        if (repository.existsByEmailAndIdNot(dto.email(), id)) {
            throw new ConflictException("Ya existe otro ciudadano con el email " + dto.email());
        }
        if (repository.existsByDniAndIdNot(dto.dni(), id)) {
            throw new ConflictException("Ya existe otro ciudadano con el DNI " + dto.dni());
        }
        mapper.updateEntity(existente, dto);
        Ciudadano guardado = repository.save(existente);
        publicar("ciudadano.actualizado", guardado);
        return mapper.toResponse(guardado);
    }

    @Transactional
    public void eliminar(Long id) {
        Ciudadano existente = buscar(id);
        repository.delete(existente);
        publicar("ciudadano.eliminado", existente);
    }

    private Ciudadano buscar(Long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Ciudadano", id));
    }

    private void publicar(String tipo, Ciudadano ciudadano) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("ciudadanoId", ciudadano.getId());
        payload.put("nombre", ciudadano.getNombre());
        payload.put("email", ciudadano.getEmail());
        publisher.publish(TOPIC, String.valueOf(ciudadano.getId()), DomainEvent.of(tipo, payload));
    }
}
