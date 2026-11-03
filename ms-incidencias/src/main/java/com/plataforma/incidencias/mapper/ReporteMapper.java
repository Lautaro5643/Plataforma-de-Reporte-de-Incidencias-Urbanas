package com.plataforma.incidencias.mapper;

import com.plataforma.incidencias.dto.ReporteRequestDTO;
import com.plataforma.incidencias.dto.ReporteResponseDTO;
import com.plataforma.incidencias.entity.EstadoReporte;
import com.plataforma.incidencias.entity.Prioridad;
import com.plataforma.incidencias.entity.Reporte;
import org.springframework.stereotype.Component;

@Component
public class ReporteMapper {

    /** Crea un reporte nuevo en estado CREADO. */
    public Reporte toEntity(ReporteRequestDTO dto) {
        Reporte reporte = new Reporte();
        reporte.setEstadoActual(EstadoReporte.CREADO);
        updateEntity(reporte, dto);
        return reporte;
    }

    /** Actualiza los datos editables; no modifica el estado ni la cuadrilla. */
    public void updateEntity(Reporte reporte, ReporteRequestDTO dto) {
        reporte.setTitulo(dto.titulo());
        reporte.setDescripcion(dto.descripcion());
        reporte.setTipoIncidencia(dto.tipoIncidencia());
        reporte.setFoto(dto.foto());
        reporte.setIdCiudadano(dto.idCiudadano());
        reporte.setIdZona(dto.idZona());
        if (dto.prioridad() != null) {
            reporte.setPrioridad(dto.prioridad());
        } else if (reporte.getPrioridad() == null) {
            reporte.setPrioridad(Prioridad.MEDIA);
        }
    }

    public ReporteResponseDTO toResponse(Reporte reporte) {
        return new ReporteResponseDTO(
                reporte.getId(),
                reporte.getTitulo(),
                reporte.getDescripcion(),
                reporte.getTipoIncidencia(),
                reporte.getFoto(),
                reporte.getPrioridad(),
                reporte.getEstadoActual(),
                reporte.getIdCiudadano(),
                reporte.getIdZona(),
                reporte.getIdCuadrilla(),
                reporte.getFechaCreacion(),
                reporte.getFechaCierre());
    }
}
