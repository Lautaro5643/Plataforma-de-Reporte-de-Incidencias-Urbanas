package com.plataforma.incidencias.dto;

import com.plataforma.incidencias.entity.Prioridad;
import com.plataforma.incidencias.entity.TipoIncidencia;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Datos para crear o actualizar un reporte. La prioridad es opcional (por defecto MEDIA);
 * el estado y la cuadrilla no se reciben aquí: se gestionan con sus propias operaciones.
 */
public record ReporteRequestDTO(
        @NotBlank(message = "El título es obligatorio")
        @Size(max = 150, message = "El título no puede superar los 150 caracteres")
        String titulo,

        @Size(max = 2000, message = "La descripción no puede superar los 2000 caracteres")
        String descripcion,

        @NotNull(message = "El tipo de incidencia es obligatorio")
        TipoIncidencia tipoIncidencia,

        @Size(max = 500, message = "La foto no puede superar los 500 caracteres")
        String foto,

        Prioridad prioridad,

        @NotNull(message = "El ciudadano es obligatorio")
        Long idCiudadano,

        @NotNull(message = "La zona es obligatoria")
        Long idZona) {
}
