package com.plataforma.zonas.mapper;

import com.plataforma.zonas.dto.ZonaRequestDTO;
import com.plataforma.zonas.dto.ZonaResponseDTO;
import com.plataforma.zonas.entity.Zona;
import org.springframework.stereotype.Component;

@Component
public class ZonaMapper {

    public Zona toEntity(ZonaRequestDTO dto) {
        Zona zona = new Zona();
        updateEntity(zona, dto);
        return zona;
    }

    public void updateEntity(Zona zona, ZonaRequestDTO dto) {
        zona.setNombre(dto.nombre());
        zona.setCodigoPostal(dto.codigoPostal());
        zona.setComuna(dto.comuna());
        zona.setLimitesGeograficos(dto.limitesGeograficos());
    }

    public ZonaResponseDTO toResponse(Zona zona) {
        return new ZonaResponseDTO(
                zona.getId(),
                zona.getNombre(),
                zona.getCodigoPostal(),
                zona.getComuna(),
                zona.getLimitesGeograficos());
    }
}
