package com.edunexus.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;

/**
 * One student's marks in one subject. Only the marks are stored; grade, SGPA and CGPA
 * are calculated from them in the service layer (Phase 9), so they can never be out of date.
 */
@Entity
@Table(name = "academic_records",
        uniqueConstraints = @UniqueConstraint(name = "uk_record_student_subject",
                columnNames = {"student_profile_id", "subject_id"}))
public class AcademicRecord extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_profile_id", nullable = false)
    private StudentProfile student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @Column(name = "marks_obtained", nullable = false, precision = 5, scale = 2)
    private BigDecimal marksObtained;

    protected AcademicRecord() {
    }

    public AcademicRecord(StudentProfile student, Subject subject, BigDecimal marksObtained) {
        this.student = student;
        this.subject = subject;
        this.marksObtained = marksObtained;
    }

    public StudentProfile getStudent() {
        return student;
    }

    public Subject getSubject() {
        return subject;
    }

    public BigDecimal getMarksObtained() {
        return marksObtained;
    }

    public void setMarksObtained(BigDecimal marksObtained) {
        this.marksObtained = marksObtained;
    }
}