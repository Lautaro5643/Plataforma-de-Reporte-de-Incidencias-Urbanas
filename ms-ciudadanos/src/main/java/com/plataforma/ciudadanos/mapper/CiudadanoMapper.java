package com.plataforma.ciudadanos.mapper;

import com.plataforma.ciudadanos.dto.CiudadanoRequestDTO;
import com.plataforma.ciudadanos.dto.CiudadanoResponseDTO;
import com.plataforma.ciudadanos.entity.Ciudadano;
import org.springframework.stereotype.Component;

@Component
public class CiudadanoMapper {

    public Ciudadano toEntity(CiudadanoRequestDTO dto) {
        Ciudadano ciudadano = new Ciudadano();
        updateEntity(ciudadano, dto);
        return ciudadano;
    }

    public void updateEntity(Ciudadano ciudadano, CiudadanoRequestDTO dto) {
        ciudadano.setNombre(dto.nombre());
        ciudadano.setEmail(dto.email());
        ciudadano.setTelefono(dto.telefono());
        ciudadano.setDni(dto.dni());
    }

    public CiudadanoResponseDTO toResponse(Ciudadano ciudadano) {
        return new CiudadanoResponseDTO(
                ciudadano.getId(),
                ciudadano.getNombre(),
                ciudadano.getEmail(),
                ciudadano.getTelefono(),
                ciudadano.getDni(),
                ciudadano.getFechaRegistro());
    }
}
