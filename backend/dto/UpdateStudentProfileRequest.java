package com.edunexus.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateStudentProfileRequest(
        @NotBlank @Size(max = 100) String fullName,
        @Size(max = 20) String phone) {}