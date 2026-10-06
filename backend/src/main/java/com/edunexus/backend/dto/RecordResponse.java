package com.edunexus.backend.dto;

import java.math.BigDecimal;

public record RecordResponse(Long id, String subjectCode, String subjectName, int semester,
                             int credits, int maxMarks, BigDecimal marksObtained,
                             BigDecimal percentage, String grade, int gradePoints,
                             boolean pass) {}