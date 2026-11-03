package com.plataforma.zonas.controller;

import com.plataforma.zonas.dto.ZonaRequestDTO;
import com.plataforma.zonas.dto.ZonaResponseDTO;
import com.plataforma.zonas.service.ZonaService;
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
@RequestMapping("/api/zonas")
public class ZonaController {

    private final ZonaService service;

    public ZonaController(ZonaService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ZonaResponseDTO> crear(@Valid @RequestBody ZonaRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(dto));
    }

    @GetMapping
    public List<ZonaResponseDTO> listar(@RequestParam(required = false) String codigoPostal) {
        return service.listar(codigoPostal);
    }

    @GetMapping("/{id}")
    public ZonaResponseDTO obtener(@PathVariable Long id) {
        return service.obtenerPorId(id);
    }

    @PutMapping("/{id}")
    public ZonaResponseDTO actualizar(@PathVariable Long id, @Valid @RequestBody ZonaRequestDTO dto) {
        return service.actualizar(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
