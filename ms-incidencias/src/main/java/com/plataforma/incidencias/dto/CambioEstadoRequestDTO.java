package com.plataforma.incidencias.dto;

import com.plataforma.incidencias.entity.EstadoReporte;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Solicitud para cambiar el estado de un reporte. */
public record CambioEstadoRequestDTO(
        @NotNull(message = "El nuevo estado es obligatorio")
        EstadoReporte nuevoEstado,

        @Size(max = 1000, message = "Las observaciones no pueden superar los 1000 caracteres")
        String observaciones,

        @Size(max = 100, message = "El usuario responsable no puede superar los 100 caracteres")
        String usuarioResponsable) {
}
