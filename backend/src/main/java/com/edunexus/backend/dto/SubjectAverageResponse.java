package com.edunexus.backend.dto;

import java.math.BigDecimal;

public record SubjectAverageResponse(String subjectCode, String subjectName, int semester,
                                     long students, BigDecimal averagePercentage,
                                     BigDecimal passRate) {}