package com.edunexus.backend.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateStatusRequest(@NotNull Boolean enabled) {}