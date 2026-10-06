package com.edunexus.backend.dto;

import java.math.BigDecimal;

/** passRate is the percentage of students who cleared every subject of that semester. */
public record SemesterPerformanceResponse(int semester, long students, BigDecimal averageSgpa,
                                          BigDecimal passRate) {}