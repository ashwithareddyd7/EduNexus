package com.edunexus.backend.dto;

import java.math.BigDecimal;

public record SemesterGpa(int semester, int credits, BigDecimal sgpa) {}