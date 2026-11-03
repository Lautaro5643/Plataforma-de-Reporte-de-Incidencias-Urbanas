package com.plataforma.incidencias.controller;

import com.plataforma.incidencias.dto.AsignarCuadrillaRequestDTO;
import com.plataforma.incidencias.dto.CambioEstadoRequestDTO;
import com.plataforma.incidencias.dto.ReporteRequestDTO;
import com.plataforma.incidencias.dto.ReporteResponseDTO;
import com.plataforma.incidencias.entity.EstadoReporte;
import com.plataforma.incidencias.service.ReporteService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reportes")
public class ReporteController {

    private final ReporteService service;

    public ReporteController(ReporteService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ReporteResponseDTO> crear(@Valid @RequestBody ReporteRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(dto));
    }

    @GetMapping
    public List<ReporteResponseDTO> listar(@RequestParam(required = false) EstadoReporte estado,
                                           @RequestParam(required = false) Long idZona,
                                           @RequestParam(required = false) Long idCiudadano) {
        return service.listar(estado, idZona, idCiudadano);
    }

    @GetMapping("/{id}")
    public ReporteResponseDTO obtener(@PathVariable Long id) {
        return service.obtenerPorId(id);
    }

    @PutMapping("/{id}")
    public ReporteResponseDTO actualizar(@PathVariable Long id, @Valid @RequestBody ReporteRequestDTO dto) {
        return service.actualizar(id, dto);
    }

    @PatchMapping("/{id}/estado")
    public ReporteResponseDTO cambiarEstado(@PathVariable Long id, @Valid @RequestBody CambioEstadoRequestDTO dto) {
        return service.cambiarEstado(id, dto);
    }

    @PutMapping("/{id}/cuadrilla")
    public ReporteResponseDTO asignarCuadrilla(@PathVariable Long id,
                                               @Valid @RequestBody AsignarCuadrillaRequestDTO dto) {
        return service.asignarCuadrilla(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
