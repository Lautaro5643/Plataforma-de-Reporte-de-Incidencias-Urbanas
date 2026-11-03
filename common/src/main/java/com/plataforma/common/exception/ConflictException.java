package com.plataforma.common.exception;

/** Se lanza cuando la operación viola una regla de unicidad o estado (HTTP 409). */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
