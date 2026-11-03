package com.plataforma.asignaciones.dto;

import com.plataforma.asignaciones.entity.Especialidad;
import com.plataforma.asignaciones.entity.EstadoCuadrilla;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Datos para crear o actualizar una cuadrilla. El estado es opcional (por defecto DISPONIBLE);
 * {@code reportesActivos} lo administra el sistema y no se recibe.
 */
public record CuadrillaRequestDTO(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
        String nombre,

        @NotNull(message = "La especialidad es obligatoria")
        Especialidad especialidad,

        @NotNull(message = "La zona es obligatoria")
        Long idZona,

        EstadoCuadrilla estado) {
}
