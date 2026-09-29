package com.edunexus.backend.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.edunexus.backend.entity.Course;
import com.edunexus.backend.entity.Department;
import com.edunexus.backend.entity.StaffProfile;
import com.edunexus.backend.entity.StudentProfile;
import com.edunexus.backend.repository.StaffProfileRepository;
import com.edunexus.backend.repository.StudentProfileRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

@ExtendWith(MockitoExtension.class)
class AccessPolicyTest {

    @Mock StudentProfileRepository studentRepo;
    @Mock StaffProfileRepository staffRepo;
    @InjectMocks AccessPolicy policy;

    private Authentication auth(long userId, String role) {
        var user = new CustomUserDetails(userId, "u" + userId + "@test.com", "hash", role, true);
        return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
    }

    private Department dept(long id) {
        Department d = mock(Department.class);
        when(d.getId()).thenReturn(id);
        return d;
    }

    /** A student (profile id 5) whose course belongs to the given department. */
    private void studentInDept(Department d) {
        Course course = mock(Course.class);
        when(course.getDepartment()).thenReturn(d);
        StudentProfile s = mock(StudentProfile.class);
        when(s.getCourse()).thenReturn(course);
        when(studentRepo.findById(5L)).thenReturn(Optional.of(s));
    }

    @Test
    void admin_canAccessAnyStudent() {
        assertTrue(policy.canAccessStudent(auth(1, "ADMIN"), 5L));
    }

    @Test
    void student_canAccessOwnProfile() {
        StudentProfile own = mock(StudentProfile.class);
        when(own.getId()).thenReturn(5L);
        when(studentRepo.findByUserId(10L)).thenReturn(Optional.of(own));
        assertTrue(policy.canAccessStudent(auth(10, "STUDENT"), 5L));
    }

    @Test
    void student_cannotAccessAnotherStudent() {   // the "change the ID in the URL" attack
        StudentProfile own = mock(StudentProfile.class);
        when(own.getId()).thenReturn(5L);
        when(studentRepo.findByUserId(10L)).thenReturn(Optional.of(own));
        assertFalse(policy.canAccessStudent(auth(10, "STUDENT"), 6L));
    }

    @Test
    void hod_canAccessStudentInOwnDepartment() {
        StaffProfile staff = mock(StaffProfile.class);
        when(staff.getDepartment()).thenReturn(dept(1L));
        when(staffRepo.findByUserId(20L)).thenReturn(Optional.of(staff));
        studentInDept(dept(1L));
        assertTrue(policy.canAccessStudent(auth(20, "HOD"), 5L));
    }

    @Test
    void hod_cannotAccessStudentInOtherDepartment() {
        StaffProfile staff = mock(StaffProfile.class);
        when(staff.getDepartment()).thenReturn(dept(1L));
        when(staffRepo.findByUserId(20L)).thenReturn(Optional.of(staff));
        studentInDept(dept(2L));
        assertFalse(policy.canAccessStudent(auth(20, "HOD"), 5L));
    }

    @Test
    void hod_withoutDepartment_isDenied() {
        StaffProfile staff = mock(StaffProfile.class);
        when(staff.getDepartment()).thenReturn(null);
        when(staffRepo.findByUserId(20L)).thenReturn(Optional.of(staff));
        assertFalse(policy.canAccessStudent(auth(20, "HOD"), 5L));
    }

    @Test
    void hod_unknownStudent_isDenied() {
        StaffProfile staff = mock(StaffProfile.class);
        when(staff.getDepartment()).thenReturn(dept(1L));
        when(staffRepo.findByUserId(20L)).thenReturn(Optional.of(staff));
        when(studentRepo.findById(99L)).thenReturn(Optional.empty());
        assertFalse(policy.canAccessStudent(auth(20, "HOD"), 99L));
    }

    @Test
    void nullOrForeignPrincipal_isDenied() {
        assertFalse(policy.canAccessStudent(null, 5L));
        assertFalse(policy.canAccessStudent(
                new UsernamePasswordAuthenticationToken("someone", null, java.util.List.of()), 5L));
    }
}