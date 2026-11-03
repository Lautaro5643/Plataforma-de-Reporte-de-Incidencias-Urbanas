package com.plataforma.zonas.dto;

public record ZonaResponseDTO(
        Long id,
        String nombre,
        String codigoPostal,
        String comuna,
        String limitesGeograficos) {
}
