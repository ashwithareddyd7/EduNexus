package com.edunexus.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * Raw OCR / extracted output for a document. Kept separate from AcademicRecord, which
 * only ever holds verified marks.
 */
@Entity
@Table(name = "document_extractions")
public class DocumentExtraction extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    @Column(name = "extracted_text", columnDefinition = "TEXT")
    private String extractedText;

    @Enumerated(EnumType.STRING)
    @Column(name = "extraction_status", nullable = false, length = 20)
    private ExtractionStatus extractionStatus = ExtractionStatus.PENDING;

    /** 0.0000 to 1.0000. */
    @Column(name = "confidence_score", precision = 5, scale = 4)
    private BigDecimal confidenceScore;

    @Column(name = "processed_at")
    private Instant processedAt;

    protected DocumentExtraction() {
    }

    public DocumentExtraction(Document document) {
        this.document = document;
    }

    public Document getDocument() {
        return document;
    }

    public String getExtractedText() {
        return extractedText;
    }

    public void setExtractedText(String extractedText) {
        this.extractedText = extractedText;
    }

    public ExtractionStatus getExtractionStatus() {
        return extractionStatus;
    }

    public void setExtractionStatus(ExtractionStatus extractionStatus) {
        this.extractionStatus = extractionStatus;
    }

    public BigDecimal getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(BigDecimal confidenceScore) {
        this.confidenceScore = confidenceScore;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(Instant processedAt) {
        this.processedAt = processedAt;
    }
}