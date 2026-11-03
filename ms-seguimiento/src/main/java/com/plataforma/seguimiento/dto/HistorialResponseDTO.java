package com.plataforma.seguimiento.dto;

import java.time.LocalDateTime;

public record HistorialResponseDTO(
        Long id,
        Long idReporte,
        String estadoAnterior,
        String estadoNuevo,
        LocalDateTime fechaCambio,
        String observaciones,
        String usuarioResponsable,
        String fotoEvidencia) {
}
