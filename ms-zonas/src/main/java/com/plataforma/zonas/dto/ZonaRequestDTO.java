package com.plataforma.zonas.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ZonaRequestDTO(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
        String nombre,

        @NotBlank(message = "El código postal es obligatorio")
        @Size(max = 10, message = "El código postal no puede superar los 10 caracteres")
        String codigoPostal,

        @NotBlank(message = "La comuna es obligatoria")
        @Size(max = 80, message = "La comuna no puede superar los 80 caracteres")
        String comuna,

        @Size(max = 4000, message = "Los límites geográficos no pueden superar los 4000 caracteres")
        String limitesGeograficos) {
}
