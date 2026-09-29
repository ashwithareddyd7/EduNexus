package com.edunexus.dto;

import java.util.List;

public record UserResponse(
        Long id,
        String fullName,
        String email,
        List<String> roles
) {}
