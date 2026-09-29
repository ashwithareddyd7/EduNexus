package com.edunexus.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * Metadata of an uploaded document. The file itself lives on disk under storedFilename
 * (a random name), so the original filename is never used as a path.
 * processingStatus tracks the OCR / extraction pipeline.
 */
@Entity
@Table(name = "documents")
public class Document extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_profile_id", nullable = false)
    private StudentProfile student;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false, length = 30)
    private DocumentType documentType;

    @Column(name = "original_filename", nullable = false, length = 255)
    private String originalFilename;

    @Column(name = "stored_filename", nullable = false, unique = true, length = 100)
    private String storedFilename;

    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    @Column(name = "size_bytes", nullable = false)
    private long sizeBytes;

    @Column(length = 255)
    private String description;

    /** Object key if the file is moved to S3 or similar; null while files live on local disk. */
    @Column(name = "storage_key", length = 512)
    private String storageKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "processing_status", nullable = false, length = 20)
    private ProcessingStatus processingStatus = ProcessingStatus.UPLOADED;

    @Column(name = "processed_at")
    private Instant processedAt;

    @Column(name = "processing_error", columnDefinition = "TEXT")
    private String processingError;

    protected Document() {
    }

    public Document(StudentProfile student, DocumentType documentType, String originalFilename,
                    String storedFilename, String contentType, long sizeBytes, String description) {
        this.student = student;
        this.documentType = documentType;
        this.originalFilename = originalFilename;
        this.storedFilename = storedFilename;
        this.contentType = contentType;
        this.sizeBytes = sizeBytes;
        this.description = description;
    }

    public StudentProfile getStudent() {
        return student;
    }

    public DocumentType getDocumentType() {
        return documentType;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public String getStoredFilename() {
        return storedFilename;
    }

    public String getContentType() {
        return contentType;
    }

    public long getSizeBytes() {
        return sizeBytes;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStorageKey() {
        return storageKey;
    }

    public void setStorageKey(String storageKey) {
        this.storageKey = storageKey;
    }

    public ProcessingStatus getProcessingStatus() {
        return processingStatus;
    }

    public void setProcessingStatus(ProcessingStatus processingStatus) {
        this.processingStatus = processingStatus;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(Instant processedAt) {
        this.processedAt = processedAt;
    }

    public String getProcessingError() {
        return processingError;
    }

    public void setProcessingError(String processingError) {
        this.processingError = processingError;
    }

    /** Convenience for the processing pipeline. */
    public void markProcessed() {
        this.processingStatus = ProcessingStatus.PROCESSED;
        this.processedAt = Instant.now();
        this.processingError = null;
    }

    public void markFailed(String error) {
        this.processingStatus = ProcessingStatus.FAILED;
        this.processedAt = Instant.now();
        this.processingError = error;
    }
}