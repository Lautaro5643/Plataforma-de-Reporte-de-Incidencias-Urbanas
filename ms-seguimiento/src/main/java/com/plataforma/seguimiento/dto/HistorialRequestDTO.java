package com.plataforma.seguimiento.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record HistorialRequestDTO(
        @NotNull(message = "El reporte es obligatorio")
        Long idReporte,

        @Size(max = 30, message = "El estado anterior no puede superar los 30 caracteres")
        String estadoAnterior,

        @NotBlank(message = "El estado nuevo es obligatorio")
        @Size(max = 30, message = "El estado nuevo no puede superar los 30 caracteres")
        String estadoNuevo,

        @Size(max = 1000, message = "Las observaciones no pueden superar los 1000 caracteres")
        String observaciones,

        @Size(max = 100, message = "El usuario responsable no puede superar los 100 caracteres")
        String usuarioResponsable,

        @Size(max = 500, message = "La foto de evidencia no puede superar los 500 caracteres")
        String fotoEvidencia) {
}
