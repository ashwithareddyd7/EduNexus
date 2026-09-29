package com.edunexus.backend.dto;

public record AuthResponse(
        String accessToken,
        String tokenType,
        long expiresInMs,
        String email,
        String role
) {}