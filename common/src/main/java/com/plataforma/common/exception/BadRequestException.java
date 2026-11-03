package com.plataforma.common.exception;

/** Se lanza cuando la solicitud es válida sintácticamente pero incumple una regla de negocio (HTTP 400). */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
