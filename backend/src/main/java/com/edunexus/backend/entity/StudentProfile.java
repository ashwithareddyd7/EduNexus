package com.edunexus.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

/**
 * Student details. Email lives on User; department comes from the course;
 * year is derived from currentSemester ((semester + 1) / 2), so none of them are stored twice.
 */
@Entity
@Table(name = "student_profiles")
public class StudentProfile extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    /** The college-issued student ID (roll number), e.g. "21CS001". Not the database primary key. */
    @Column(name = "student_id", nullable = false, unique = true, length = 30)
    private String studentId;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(length = 20)
    private String phone;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(name = "current_semester", nullable = false)
    private int currentSemester;

    protected StudentProfile() {
    }

    public StudentProfile(User user, String studentId, String fullName, Course course, int currentSemester) {
        this.user = user;
        this.studentId = studentId;
        this.fullName = fullName;
        this.course = course;
        this.currentSemester = currentSemester;
    }

    public User getUser() {
        return user;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public int getCurrentSemester() {
        return currentSemester;
    }

    public void setCurrentSemester(int currentSemester) {
        this.currentSemester = currentSemester;
    }
}