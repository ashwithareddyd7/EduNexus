package com.edunexus.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/** Semester N of a course (e.g. semester 3 of B.Tech CSE). Each number appears once per course. */
@Entity
@Table(name = "semesters",
        uniqueConstraints = @UniqueConstraint(name = "uk_semester_course_number",
                columnNames = {"course_id", "semester_number"}))
public class Semester extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(name = "semester_number", nullable = false)
    private int semesterNumber;

    protected Semester() {
    }

    public Semester(Course course, int semesterNumber) {
        this.course = course;
        this.semesterNumber = semesterNumber;
    }

    public Course getCourse() {
        return course;
    }

    public int getSemesterNumber() {
        return semesterNumber;
    }
}