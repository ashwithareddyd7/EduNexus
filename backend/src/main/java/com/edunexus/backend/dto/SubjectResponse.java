package com.edunexus.backend.dto;

public record SubjectResponse(Long id, String code, String name, int credits,
                              int maxMarks, int semester, Long courseId) {}