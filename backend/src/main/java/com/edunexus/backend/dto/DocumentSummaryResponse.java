package com.edunexus.backend.dto;

import com.edunexus.backend.entity.DocumentType;

public record DocumentSummaryResponse(Long id, Long studentProfileId, String rollNumber,
                                      String studentName, String courseName,
                                      DocumentType documentType, String originalFilename,
                                      String contentType, long sizeBytes, String description) {}