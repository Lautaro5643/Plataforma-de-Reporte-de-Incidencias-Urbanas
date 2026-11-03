package com.plataforma.asignaciones.dto;

import com.plataforma.asignaciones.entity.Especialidad;
import com.plataforma.asignaciones.entity.EstadoCuadrilla;

public record CuadrillaResponseDTO(
        Long id,
        String nombre,
        Especialidad especialidad,
        Long idZona,
        EstadoCuadrilla estado,
        int reportesActivos) {
}
