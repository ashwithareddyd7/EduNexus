package com.edunexus.backend.exception;

/** Thrown when an action conflicts with existing data (e.g. duplicate email). Mapped to HTTP 409. */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}