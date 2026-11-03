package com.plataforma.seguimiento.controller;

import com.plataforma.seguimiento.dto.HistorialRequestDTO;
import com.plataforma.seguimiento.dto.HistorialResponseDTO;
import com.plataforma.seguimiento.service.HistorialService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/seguimientos")
public class HistorialController {

    private final HistorialService service;

    public HistorialController(HistorialService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<HistorialResponseDTO> registrar(@Valid @RequestBody HistorialRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.registrar(dto));
    }

    @GetMapping
    public List<HistorialResponseDTO> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public HistorialResponseDTO obtener(@PathVariable Long id) {
        return service.obtenerPorId(id);
    }

    @GetMapping("/reporte/{idReporte}")
    public List<HistorialResponseDTO> listarPorReporte(@PathVariable Long idReporte) {
        return service.listarPorReporte(idReporte);
    }
}
