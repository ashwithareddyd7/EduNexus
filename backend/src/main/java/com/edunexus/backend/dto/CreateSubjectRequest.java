package com.edunexus.backend.dto;

import jakarta.validation.constraints.*;

public record CreateSubjectRequest(
        @NotNull Long courseId,
        @Min(1) @Max(12) int semesterNumber,
        @NotBlank @Size(max = 20) String code,
        @NotBlank @Size(max = 150) String name,
        @Min(1) @Max(10) int credits,
        @Min(1) @Max(999) int maxMarks) {}