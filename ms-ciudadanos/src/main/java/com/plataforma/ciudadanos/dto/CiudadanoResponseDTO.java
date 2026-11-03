package com.plataforma.ciudadanos.dto;

import java.time.LocalDateTime;

public record CiudadanoResponseDTO(
        Long id,
        String nombre,
        String email,
        String telefono,
        String dni,
        LocalDateTime fechaRegistro) {
}
