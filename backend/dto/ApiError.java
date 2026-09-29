package com.edunexus.backend.dto;

import java.time.Instant;
import java.util.List;

/**
 * The single JSON shape returned for every error.
 * fieldErrors is only present for validation failures (null fields are omitted from the JSON).
 */
public record ApiError(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        List<FieldViolation> fieldErrors) {

    /** One invalid field, e.g. field="email", message="must be a well-formed email address". */
    public record FieldViolation(String field, String message) {
    }
}