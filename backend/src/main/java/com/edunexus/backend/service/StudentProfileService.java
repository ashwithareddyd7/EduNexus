package com.edunexus.backend.service;

import com.edunexus.backend.dto.CreateStudentProfileRequest;
import com.edunexus.backend.dto.StudentProfileResponse;
import com.edunexus.backend.dto.UpdateStudentProfileRequest;
import com.edunexus.backend.entity.Course;
import com.edunexus.backend.entity.StaffProfile;
import com.edunexus.backend.entity.StudentProfile;
import com.edunexus.backend.entity.User;
import com.edunexus.backend.exception.BadRequestException;
import com.edunexus.backend.exception.ConflictException;
import com.edunexus.backend.exception.ResourceNotFoundException;
import com.edunexus.backend.repository.CourseRepository;
import com.edunexus.backend.repository.StaffProfileRepository;
import com.edunexus.backend.repository.StudentProfileRepository;
import com.edunexus.backend.repository.UserRepository;
import com.edunexus.backend.security.CustomUserDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudentProfileService {

    private final StudentProfileRepository profiles;
    private final StaffProfileRepository staff;
    private final UserRepository users;
    private final CourseRepository courses;

    public StudentProfileService(StudentProfileRepository profiles,
                                 StaffProfileRepository staff,
                                 UserRepository users,
                                 CourseRepository courses) {
        this.profiles = profiles;
        this.staff = staff;
        this.users = users;
        this.courses = courses;
    }

    // ---------- Student: own profile ----------

    @Transactional
    public StudentProfileResponse createMine(Long userId, CreateStudentProfileRequest req) {
        if (profiles.findByUserId(userId).isPresent()) {
            throw new ConflictException("Profile already exists");
        }
        if (profiles.existsByStudentId(req.studentId())) {
            throw new ConflictException("Student ID already in use");
        }
        Course course = courses.findById(req.courseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
        if (req.currentSemester() > course.getTotalSemesters()) {
            throw new BadRequestException("Semester exceeds the length of the course");
        }
        User user = users.getReferenceById(userId);
        StudentProfile saved = profiles.save(new StudentProfile(
                user, req.studentId(), req.fullName(), course, req.currentSemester()));
        saved.setPhone(req.phone());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public StudentProfileResponse getMine(Long userId) {
        return toResponse(findByUser(userId));
    }

    @Transactional
    public StudentProfileResponse updateMine(Long userId, UpdateStudentProfileRequest req) {
        StudentProfile p = findByUser(userId);
        p.setFullName(req.fullName());
        p.setPhone(req.phone());
        return toResponse(p);
    }

    // ---------- Staff and student: view by id ----------

    @Transactional(readOnly = true)
    public StudentProfileResponse getById(Long id, CustomUserDetails me) {
        StudentProfile p = profiles.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        assertCanView(me, p);
        return toResponse(p);
    }

    // ---------- HOD and admin: list ----------

    @Transactional(readOnly = true)
    public Page<StudentProfileResponse> list(CustomUserDetails me, Pageable pageable) {
        return switch (me.getRole()) {
            case "ADMIN" -> profiles.findAll(pageable).map(this::toResponse);
            case "HOD" -> profiles.findByCourseDepartmentId(hodDepartmentId(me), pageable)
                    .map(this::toResponse);
            default -> throw new AccessDeniedException("You do not have permission to list students");
        };
    }

    // ---------- Helpers ----------

    private StudentProfile findByUser(Long userId) {
        return profiles.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));
    }

    public void assertCanView(CustomUserDetails me, StudentProfile p) {
        boolean allowed = switch (me.getRole()) {
            case "ADMIN" -> true;
            case "HOD" -> hodDepartmentId(me).equals(p.getCourse().getDepartment().getId());
            case "STUDENT" -> p.getUser().getId().equals(me.getId());
            default -> false;
        };
        if (!allowed) {
            throw new AccessDeniedException("You do not have permission to view this student");
        }
    }

    private Long hodDepartmentId(CustomUserDetails me) {
        StaffProfile s = staff.findByUserId(me.getId())
                .orElseThrow(() -> new AccessDeniedException("No staff profile found"));
        if (s.getDepartment() == null) {
            throw new AccessDeniedException("No department assigned");
        }
        return s.getDepartment().getId();
    }

    private StudentProfileResponse toResponse(StudentProfile p) {
        Course c = p.getCourse();
        return new StudentProfileResponse(
                p.getId(),
                p.getStudentId(),
                p.getFullName(),
                p.getUser().getEmail(),
                p.getPhone(),
                c.getId(),
                c.getName(),
                c.getDepartment().getName(),
                p.getCurrentSemester(),
                (p.getCurrentSemester() + 1) / 2,
                p.getUser().isEnabled());
    }

    public void assertCanManage(CustomUserDetails me, Long departmentId) {
        boolean allowed = switch (me.getRole()) {
            case "ADMIN" -> true;
            case "HOD" -> hodDepartmentId(me).equals(departmentId);
            default -> false;
        };
        if (!allowed) throw new AccessDeniedException("Not allowed");
    }}
