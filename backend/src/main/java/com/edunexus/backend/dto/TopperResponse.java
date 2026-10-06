package com.edunexus.backend.dto;

import java.math.BigDecimal;

/** gpa is the SGPA when a semester was requested, otherwise the CGPA. Tied students share a rank. */
public record TopperResponse(int rank, Long studentProfileId, String rollNumber, String fullName,
                             String courseName, BigDecimal gpa, int failedSubjects) {}