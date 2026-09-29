package com.edunexus.backend.dto;

public record UserResponse(
        Long id,
        String email,
        String role
) {}