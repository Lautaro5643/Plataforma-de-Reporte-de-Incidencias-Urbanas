package com.plataforma.asignaciones.mapper;

import com.plataforma.asignaciones.dto.CuadrillaRequestDTO;
import com.plataforma.asignaciones.dto.CuadrillaResponseDTO;
import com.plataforma.asignaciones.entity.Cuadrilla;
import com.plataforma.asignaciones.entity.EstadoCuadrilla;
import org.springframework.stereotype.Component;

@Component
public class CuadrillaMapper {

    public Cuadrilla toEntity(CuadrillaRequestDTO dto) {
        Cuadrilla cuadrilla = new Cuadrilla();
        cuadrilla.setReportesActivos(0);
        cuadrilla.setEstado(EstadoCuadrilla.DISPONIBLE);
        updateEntity(cuadrilla, dto);
        return cuadrilla;
    }

    /** Actualiza los datos editables; el estado solo cambia si se envía y los reportes activos no se tocan. */
    public void updateEntity(Cuadrilla cuadrilla, CuadrillaRequestDTO dto) {
        cuadrilla.setNombre(dto.nombre());
        cuadrilla.setEspecialidad(dto.especialidad());
        cuadrilla.setIdZona(dto.idZona());
        if (dto.estado() != null) {
            cuadrilla.setEstado(dto.estado());
        }
    }

    public CuadrillaResponseDTO toResponse(Cuadrilla cuadrilla) {
        return new CuadrillaResponseDTO(
                cuadrilla.getId(),
                cuadrilla.getNombre(),
                cuadrilla.getEspecialidad(),
                cuadrilla.getIdZona(),
                cuadrilla.getEstado(),
                cuadrilla.getReportesActivos());
    }
}
