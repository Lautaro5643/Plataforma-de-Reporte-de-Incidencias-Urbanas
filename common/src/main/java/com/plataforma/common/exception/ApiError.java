package com.plataforma.common.exception;

import java.time.LocalDateTime;
import java.util.List;

/** Cuerpo estándar de las respuestas de error de todos los microservicios. */
public record ApiError(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        List<String> details) {
}
