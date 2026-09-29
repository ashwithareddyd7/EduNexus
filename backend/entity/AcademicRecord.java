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
 * One student's marks in one subject for one assessment (e.g. MIDTERM, FINAL) in one academic year.
 * Only the marks are stored; grade, SGPA and CGPA are calculated from them in the service layer
 * (Phase 9), so they can never be out of date.
 */
@Entity
@Table(name = "academic_records",
        uniqueConstraints = @UniqueConstraint(name = "uk_record_student_subject_exam",
                columnNames = {"student_profile_id", "subject_id", "academic_year",
                        "assessment_type", "attempt_number"}))
public class AcademicRecord extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_profile_id", nullable = false)
    private StudentProfile student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @Column(name = "marks_obtained", nullable = false, precision = 5, scale = 2)
    private BigDecimal marksObtained;

    /** Format "2025-2026". */
    @Column(name = "academic_year", length = 9)
    private String academicYear;

    /** e.g. MIDTERM, FINAL, INTERNAL. */
    @Column(name = "assessment_type", length = 30)
    private String assessmentType;

    @Column(name = "attempt_number", nullable = false)
    private int attemptNumber = 1;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
@JoinColumn(name = "semester_id", nullable = false)
private Semester semester;

    protected AcademicRecord() {
    }

    public AcademicRecord(StudentProfile student, Subject subject, BigDecimal marksObtained) {
        this.student = student;
        this.subject = subject;
        this.marksObtained = marksObtained;
    }

    public AcademicRecord(StudentProfile student, Subject subject, BigDecimal marksObtained,
                          String academicYear, String assessmentType, int attemptNumber) {
        this(student, subject, marksObtained);
        this.academicYear = academicYear;
        this.assessmentType = assessmentType;
        this.attemptNumber = attemptNumber;
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

    public String getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(String academicYear) {
        this.academicYear = academicYear;
    }

    public String getAssessmentType() {
        return assessmentType;
    }

    public void setAssessmentType(String assessmentType) {
        this.assessmentType = assessmentType;
    }

    public int getAttemptNumber() {
        return attemptNumber;
    }

    public void setAttemptNumber(int attemptNumber) {
        this.attemptNumber = attemptNumber;
    }
}