
package com.edunexus.backend.dto;

import java.time.Instant;

/**
 * Simple response object for the health-check endpoint.
 * DTOs like this (never JPA entities) are what our APIs return.
 */
public record HealthResponse(String status, String service, Instant timestamp) {
}