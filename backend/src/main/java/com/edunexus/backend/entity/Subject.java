package com.edunexus.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** A subject taught in one semester of a course. Credits are used later to compute SGPA/CGPA. */
@Entity
@Table(name = "subjects")
public class Subject extends BaseEntity {

    @Column(nullable = false, unique = true, length = 20)
    private String code;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false)
    private int credits;

    @Column(name = "max_marks", nullable = false)
    private int maxMarks;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "semester_id", nullable = false)
    private Semester semester;

    protected Subject() {
    }

    public Subject(String code, String name, int credits, int maxMarks, Semester semester) {
        this.code = code;
        this.name = name;
        this.credits = credits;
        this.maxMarks = maxMarks;
        this.semester = semester;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCredits() {
        return credits;
    }

    public void setCredits(int credits) {
        this.credits = credits;
    }

    public int getMaxMarks() {
        return maxMarks;
    }

    public void setMaxMarks(int maxMarks) {
        this.maxMarks = maxMarks;
    }

    public Semester getSemester() {
        return semester;
    }
}