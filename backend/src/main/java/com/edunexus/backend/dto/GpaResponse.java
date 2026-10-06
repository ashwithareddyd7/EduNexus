package com.edunexus.backend.dto;

import java.math.BigDecimal;
import java.util.List;

public record GpaResponse(List<SemesterGpa> semesters, BigDecimal cgpa) {}