package com.plataforma.seguimiento.mapper;

import com.plataforma.seguimiento.dto.HistorialRequestDTO;
import com.plataforma.seguimiento.dto.HistorialResponseDTO;
import com.plataforma.seguimiento.entity.HistorialSeguimiento;
import org.springframework.stereotype.Component;

@Component
public class HistorialMapper {

    public HistorialSeguimiento toEntity(HistorialRequestDTO dto) {
        HistorialSeguimiento historial = new HistorialSeguimiento();
        historial.setIdReporte(dto.idReporte());
        historial.setEstadoAnterior(dto.estadoAnterior());
        historial.setEstadoNuevo(dto.estadoNuevo());
        historial.setObservaciones(dto.observaciones());
        historial.setUsuarioResponsable(dto.usuarioResponsable());
        historial.setFotoEvidencia(dto.fotoEvidencia());
        return historial;
    }

    public HistorialResponseDTO toResponse(HistorialSeguimiento historial) {
        return new HistorialResponseDTO(
                historial.getId(),
                historial.getIdReporte(),
                historial.getEstadoAnterior(),
                historial.getEstadoNuevo(),
                historial.getFechaCambio(),
                historial.getObservaciones(),
                historial.getUsuarioResponsable(),
                historial.getFotoEvidencia());
    }
}
