package com.edunexus.backend.dto;

public record CourseResponse(
        Long id,
        String code,
        String name,
        String departmentName,
        int totalSemesters) {}