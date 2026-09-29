package com.edunexus.backend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import org.springframework.beans.BeanUtils;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;

import com.edunexus.backend.dto.CreateStudentProfileRequest;
import com.edunexus.backend.entity.*;
import com.edunexus.backend.exception.BadRequestException;
import com.edunexus.backend.exception.ConflictException;
import com.edunexus.backend.repository.*;
import com.edunexus.backend.security.CustomUserDetails;

@ExtendWith(MockitoExtension.class)
class StudentProfileServiceTest {

    @Mock StudentProfileRepository profiles;
    @Mock StaffProfileRepository staff;
    @Mock UserRepository users;
    @Mock CourseRepository courses;
    @InjectMocks StudentProfileService service;

    // ---------- helpers: real objects, NO when() inside ----------

    private Department dept(long id) {
        Department d = new Department("D" + id, "Department " + id);
        ReflectionTestUtils.setField(d, "id", id);
        return d;
    }

    private Course course(Department d, int semesters) {
        Course c = new Course("C-" + d.getId(), "Course " + d.getId(), d, semesters);
        ReflectionTestUtils.setField(c, "id", 1L);
        return c;
    }

    private User user(long id) {
    User u = BeanUtils.instantiateClass(User.class);
    ReflectionTestUtils.setField(u, "id", id);
    return u;
}

    private StudentProfile profile(long ownerUserId, long deptId) {
        Course c = course(dept(deptId), 8);
        StudentProfile p = new StudentProfile(user(ownerUserId), "21CS001", "Asha Rao", c, 3);
        ReflectionTestUtils.setField(p, "id", 10L);
        return p;
    }

   private StaffProfile hodStaff(Department d) {
    StaffProfile s = BeanUtils.instantiateClass(StaffProfile.class);
    ReflectionTestUtils.setField(s, "department", d);
    return s;
}

    private CustomUserDetails principal(long id, String role) {
        return new CustomUserDetails(id, "x@test.com", "hash", role, true);
    }

    // ---------- view rules ----------

    @Test
    void studentCannotViewAnotherStudent() {
        StudentProfile other = profile(2L, 1L);
        when(profiles.findById(10L)).thenReturn(Optional.of(other));
        CustomUserDetails me = principal(1L, "STUDENT");

        assertThrows(AccessDeniedException.class, () -> service.getById(10L, me));
    }

    @Test
    void studentCanViewOwnProfile() {
        StudentProfile mine = profile(1L, 1L);
        when(profiles.findById(10L)).thenReturn(Optional.of(mine));
        CustomUserDetails me = principal(1L, "STUDENT");

        assertNotNull(service.getById(10L, me));
    }

    @Test
    void hodCanViewOwnDepartment() {
        StudentProfile p = profile(2L, 1L);
        StaffProfile hod = hodStaff(dept(1L));
        when(profiles.findById(10L)).thenReturn(Optional.of(p));
        when(staff.findByUserId(5L)).thenReturn(Optional.of(hod));
        CustomUserDetails me = principal(5L, "HOD");

        assertNotNull(service.getById(10L, me));
    }

    @Test
    void hodCannotViewOtherDepartment() {
        StudentProfile p = profile(2L, 2L);          // student in dept 2
        StaffProfile hod = hodStaff(dept(1L));       // HOD of dept 1
        when(profiles.findById(10L)).thenReturn(Optional.of(p));
        when(staff.findByUserId(5L)).thenReturn(Optional.of(hod));
        CustomUserDetails me = principal(5L, "HOD");

        assertThrows(AccessDeniedException.class, () -> service.getById(10L, me));
    }

    @Test
    void hodWithoutDepartmentIsDenied() {
        StudentProfile p = profile(2L, 1L);
        StaffProfile hod = hodStaff(null);
        when(profiles.findById(10L)).thenReturn(Optional.of(p));
        when(staff.findByUserId(5L)).thenReturn(Optional.of(hod));
        CustomUserDetails me = principal(5L, "HOD");

        assertThrows(AccessDeniedException.class, () -> service.getById(10L, me));
    }

    @Test
    void adminCanViewAnyone() {
        StudentProfile p = profile(2L, 2L);
        when(profiles.findById(10L)).thenReturn(Optional.of(p));
        CustomUserDetails me = principal(99L, "ADMIN");

        assertNotNull(service.getById(10L, me));
    }

    @Test
    void studentCannotListStudents() {
        CustomUserDetails me = principal(1L, "STUDENT");

        assertThrows(AccessDeniedException.class,
                () -> service.list(me, PageRequest.of(0, 10)));
    }

    // ---------- create rules ----------

    @Test
    void createMineRejectsDuplicateProfile() {
        StudentProfile existing = profile(1L, 1L);
        when(profiles.findByUserId(1L)).thenReturn(Optional.of(existing));
        CreateStudentProfileRequest req =
                new CreateStudentProfileRequest("21CS002", "Asha Rao", "9999999999", 1L, 3);

        assertThrows(ConflictException.class, () -> service.createMine(1L, req));
        verify(profiles, never()).save(any());
    }

    @Test
    void createMineRejectsDuplicateStudentId() {
        when(profiles.findByUserId(1L)).thenReturn(Optional.empty());
        when(profiles.existsByStudentId("21CS001")).thenReturn(true);
        CreateStudentProfileRequest req =
                new CreateStudentProfileRequest("21CS001", "Asha Rao", "9999999999", 1L, 3);

        assertThrows(ConflictException.class, () -> service.createMine(1L, req));
    }

    @Test
    void createMineRejectsSemesterBeyondCourse() {
        Course c = course(dept(1L), 8);
        when(profiles.findByUserId(1L)).thenReturn(Optional.empty());
        when(profiles.existsByStudentId("21CS001")).thenReturn(false);
        when(courses.findById(1L)).thenReturn(Optional.of(c));
        CreateStudentProfileRequest req =
                new CreateStudentProfileRequest("21CS001", "Asha Rao", "9999999999", 1L, 9);

        assertThrows(BadRequestException.class, () -> service.createMine(1L, req));
    }
 
}