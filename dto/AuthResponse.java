package com.edunexus.dto;

import java.util.List;

public record AuthResponse(
        String accessToken,
        String tokenType,
        long expiresInMs,
        String email,
        List<String> roles
) {}