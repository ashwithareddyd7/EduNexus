package com.edunexus.backend.dto;

public record StudentProfileResponse(
        Long id,
        String studentId,
        String fullName,
        String email,
        String phone,
        Long courseId,
        String courseName,
        String departmentName,
        int currentSemester,
        int year) {}