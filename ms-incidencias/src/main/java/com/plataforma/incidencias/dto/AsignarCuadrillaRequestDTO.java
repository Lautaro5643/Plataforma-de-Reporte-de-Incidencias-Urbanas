package com.plataforma.incidencias.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Solicitud para asignar una cuadrilla a un reporte. */
public record AsignarCuadrillaRequestDTO(
        @NotNull(message = "La cuadrilla es obligatoria")
        Long idCuadrilla,

        @Size(max = 100, message = "El usuario responsable no puede superar los 100 caracteres")
        String usuarioResponsable) {
}
