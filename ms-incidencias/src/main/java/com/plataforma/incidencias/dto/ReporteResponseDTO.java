package com.plataforma.incidencias.dto;

import com.plataforma.incidencias.entity.EstadoReporte;
import com.plataforma.incidencias.entity.Prioridad;
import com.plataforma.incidencias.entity.TipoIncidencia;
import java.time.LocalDateTime;

public record ReporteResponseDTO(
        Long id,
        String titulo,
        String descripcion,
        TipoIncidencia tipoIncidencia,
        String foto,
        Prioridad prioridad,
        EstadoReporte estadoActual,
        Long idCiudadano,
        Long idZona,
        Long idCuadrilla,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaCierre) {
}
