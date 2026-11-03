package com.plataforma.ciudadanos.controller;

import com.plataforma.ciudadanos.dto.CiudadanoRequestDTO;
import com.plataforma.ciudadanos.dto.CiudadanoResponseDTO;
import com.plataforma.ciudadanos.service.CiudadanoService;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ciudadanos")
public class CiudadanoController {

    private final CiudadanoService service;

    public CiudadanoController(CiudadanoService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<CiudadanoResponseDTO> crear(@Valid @RequestBody CiudadanoRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(dto));
    }

    @GetMapping
    public List<CiudadanoResponseDTO> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public CiudadanoResponseDTO obtener(@PathVariable Long id) {
        return service.obtenerPorId(id);
    }

    @GetMapping("/dni/{dni}")
    public CiudadanoResponseDTO obtenerPorDni(@PathVariable String dni) {
        return service.obtenerPorDni(dni);
    }

    @PutMapping("/{id}")
    public CiudadanoResponseDTO actualizar(@PathVariable Long id, @Valid @RequestBody CiudadanoRequestDTO dto) {
        return service.actualizar(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
