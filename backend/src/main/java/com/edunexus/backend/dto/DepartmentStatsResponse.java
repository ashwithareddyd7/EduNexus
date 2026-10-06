package com.edunexus.backend.dto;

import java.math.BigDecimal;

/**
 * studentsWithMarks counts students with at least one mark. averageCgpa is over those students.
 * passRate is the percentage of those students with no failed subject so far.
 */
public record DepartmentStatsResponse(Long departmentId, String departmentName, long students,
                                      long studentsWithMarks, BigDecimal averageCgpa,
                                      BigDecimal passRate) {}