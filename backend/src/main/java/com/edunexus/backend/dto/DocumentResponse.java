package com.edunexus.backend.dto;

import com.edunexus.backend.entity.DocumentType;

public record DocumentResponse(Long id, DocumentType documentType, String originalFilename,
                               String contentType, long sizeBytes, String description) {}