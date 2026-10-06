package com.edunexus.backend.dto;

import java.util.List;

/** departmentId is null (and departmentName is "All departments") for an admin who did not pick one. */
public record OverviewResponse(Long departmentId, String departmentName, long totalStudents,
                               long totalDocuments, List<CourseCount> studentsPerCourse) {}