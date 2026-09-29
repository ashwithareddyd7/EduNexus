package com.edunexus.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** A faculty remark about a student, optionally tied to one subject. */
@Entity
@Table(name = "remarks")
public class Remark extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_profile_id", nullable = false)
    private StudentProfile student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "staff_profile_id", nullable = false)
    private StaffProfile staff;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id")
    private Subject subject;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    protected Remark() {
    }

    public Remark(StudentProfile student, StaffProfile staff, Subject subject, String message) {
        this.student = student;
        this.staff = staff;
        this.subject = subject;
        this.message = message;
    }

    public StudentProfile getStudent() {
        return student;
    }

    public StaffProfile getStaff() {
        return staff;
    }

    public Subject getSubject() {
        return subject;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}