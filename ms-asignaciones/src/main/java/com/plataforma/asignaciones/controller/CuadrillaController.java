package com.plataforma.asignaciones.controller;

import com.plataforma.asignaciones.dto.CuadrillaRequestDTO;
import com.plataforma.asignaciones.dto.CuadrillaResponseDTO;
import com.plataforma.asignaciones.entity.Especialidad;
import com.plataforma.asignaciones.entity.EstadoCuadrilla;
import com.plataforma.asignaciones.service.CuadrillaService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cuadrillas")
public class CuadrillaController {

    private final CuadrillaService service;

    public CuadrillaController(CuadrillaService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<CuadrillaResponseDTO> crear(@Valid @RequestBody CuadrillaRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(dto));
    }

    @GetMapping
    public List<CuadrillaResponseDTO> listar(@RequestParam(required = false) Long idZona,
                                             @RequestParam(required = false) EstadoCuadrilla estado) {
        return service.listar(idZona, estado);
    }

    @GetMapping("/asignacion-sugerida")
    public CuadrillaResponseDTO sugerir(@RequestParam Long idZona, @RequestParam Especialidad especialidad) {
        return service.sugerirCuadrilla(idZona, especialidad);
    }

    @GetMapping("/{id}")
    public CuadrillaResponseDTO obtener(@PathVariable Long id) {
        return service.obtenerPorId(id);
    }

    @PutMapping("/{id}")
    public CuadrillaResponseDTO actualizar(@PathVariable Long id, @Valid @RequestBody CuadrillaRequestDTO dto) {
        return service.actualizar(id, dto);
    }

    @PostMapping("/{id}/asignar-reporte")
    public CuadrillaResponseDTO asignarReporte(@PathVariable Long id) {
        return service.asignarReporte(id);
    }

    @PostMapping("/{id}/liberar-reporte")
    public CuadrillaResponseDTO liberarReporte(@PathVariable Long id) {
        return service.liberarReporte(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
