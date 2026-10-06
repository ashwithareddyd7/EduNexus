package com.edunexus.backend.dto;

public record StudentStatusResponse(Long id, String studentId, String fullName, String email,
                                    boolean enabled) {}