package com.edunexus.backend.exception;

/** Thrown when a request is well-formed but breaks a business rule. Mapped to HTTP 400. */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}