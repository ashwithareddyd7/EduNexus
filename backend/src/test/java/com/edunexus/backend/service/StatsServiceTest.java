package com.edunexus.backend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;
import com.edunexus.backend.dto.*;
import com.edunexus.backend.entity.*;
import com.edunexus.backend.repository.CourseRepository;
import com.edunexus.backend.repository.StaffProfileRepository;
import com.edunexus.backend.repository.StatsRepository;
import com.edunexus.backend.security.CustomUserDetails;

@ExtendWith(MockitoExtension.class)
class StatsServiceTest {

    @Mock StatsRepository stats;
    @Mock CourseRepository courses;
    @Mock StaffProfileRepository staff;
    @Mock StudentProfileService studentService;

    StatsService service;

    private final Department dept = withId(new Department("CSE", "Computer Science"), 5L);
    private final Course course = withId(new Course("BTECH-CSE", "B.Tech CSE", dept, 8), 23L);
    private final Semester sem1 = withId(new Semester(course, 1), 1L);
    private final Semester sem2 = withId(new Semester(course, 2), 2L);
    private final Subject sub1 = withId(new Subject("S1", "Maths", 4, 100, sem1), 11L);
    private final Subject sub2 = withId(new Subject("S2", "Physics", 4, 100, sem2), 12L);

    @BeforeEach
    void setUp() {
        service = new StatsService(stats, courses, studentService, new StaffScope(staff, stats));
    }

    private static <T> T withId(T entity, Long id) {
        ReflectionTestUtils.setField(entity, "id", id);
        return entity;
    }

    private StudentProfile student(long id, String roll, String name) {
        return withId(new StudentProfile(null, roll, name, course, 1), id);
    }

    private static AcademicRecord rec(StudentProfile s, Subject sub, String marks) {
        return new AcademicRecord(s, sub, new BigDecimal(marks));
    }

    private static CustomUserDetails principal(long id, String role) {
        return new CustomUserDetails(id, "x@test.com", "hash", role, true);
    }

    private static void assertDecimal(String expected, BigDecimal actual) {
        assertEquals(0, new BigDecimal(expected).compareTo(actual), "expected " + expected + " but was " + actual);
    }

    // ---------- toppers ----------

    @Test
    void toppersAreRankedByCgpaAndTiesShareARank() {
        StudentProfile a = student(1, "21CS001", "Alice");
        StudentProfile b = student(2, "21CS002", "Bob");
        StudentProfile c = student(3, "21CS003", "Cara");
        StudentProfile d = student(4, "21CS004", "Dev");
        when(stats.findAllWithGraph()).thenReturn(List.of(
                rec(a, sub1, "95"), rec(b, sub1, "95"), rec(c, sub1, "55"), rec(d, sub1, "30")));

        List<TopperResponse> t = service.toppers(principal(99L, "ADMIN"), null, null, null, 10);

        assertEquals(List.of(1, 1, 3, 4), t.stream().map(TopperResponse::rank).toList());
        assertEquals("Alice", t.get(0).fullName());
        assertEquals("Bob", t.get(1).fullName());
        assertDecimal("10.00", t.get(0).gpa());
        assertDecimal("6.00", t.get(2).gpa());
        assertEquals(1, t.get(3).failedSubjects());
    }

    @Test
    void toppersRespectsTheLimit() {
        StudentProfile a = student(1, "21CS001", "Alice");
        StudentProfile b = student(2, "21CS002", "Bob");
        StudentProfile c = student(3, "21CS003", "Cara");
        when(stats.findAllWithGraph()).thenReturn(List.of(
                rec(a, sub1, "95"), rec(b, sub1, "95"), rec(c, sub1, "55")));

        List<TopperResponse> t = service.toppers(principal(99L, "ADMIN"), null, null, null, 2);

        assertEquals(2, t.size());
    }

    @Test
    void toppersForOneSemesterIgnoresOtherSemesters() {
        StudentProfile a = student(1, "21CS001", "Alice");
        StudentProfile b = student(2, "21CS002", "Bob");
        when(stats.findAllWithGraph()).thenReturn(List.of(
                rec(a, sub1, "95"), rec(b, sub2, "95")));

        List<TopperResponse> t = service.toppers(principal(99L, "ADMIN"), null, null, 2, 10);

        assertEquals(1, t.size());
        assertEquals("Bob", t.get(0).fullName());
    }

    @Test
    void toppersWithACourseUsesTheCourseQueryAndChecksManageAccess() {
        CustomUserDetails admin = principal(99L, "ADMIN");
        when(courses.findById(23L)).thenReturn(Optional.of(course));
        when(stats.findByCourseWithGraph(23L)).thenReturn(List.of());

        assertTrue(service.toppers(admin, null, 23L, null, 10).isEmpty());

        verify(studentService).assertCanManage(admin, 5L);
        verify(stats, never()).findAllWithGraph();
    }

    @Test
    void rejectsAnInvalidSemester() {
        CustomUserDetails admin = principal(99L, "ADMIN");
        assertThrows(com.edunexus.backend.exception.BadRequestException.class,
                () -> service.toppers(admin, null, null, 13, 10));
    }

    // ---------- scoping ----------

    @Test
    void hodIsAlwaysScopedToTheirOwnDepartment() {
        when(staff.findByUserId(7L)).thenReturn(Optional.of(new StaffProfile(null, "HOD One", dept)));
        when(stats.findByDepartmentWithGraph(5L)).thenReturn(List.of());

        assertTrue(service.toppers(principal(7L, "HOD"), null, null, null, 10).isEmpty());

        verify(stats, never()).findAllWithGraph();
    }

    @Test
    void hodCannotAskForAnotherDepartment() {
        when(staff.findByUserId(7L)).thenReturn(Optional.of(new StaffProfile(null, "HOD One", dept)));
        CustomUserDetails hod = principal(7L, "HOD");

        assertThrows(AccessDeniedException.class, () -> service.toppers(hod, 6L, null, null, 10));

        verify(stats, never()).findAllWithGraph();
        verify(stats, never()).findByDepartmentWithGraph(any());
    }

    @Test
    void hodWithoutADepartmentIsRefused() {
        when(staff.findByUserId(7L)).thenReturn(Optional.of(new StaffProfile(null, "HOD One", null)));
        CustomUserDetails hod = principal(7L, "HOD");

        assertThrows(AccessDeniedException.class, () -> service.overview(hod, null));
    }

    @Test
    void studentsCannotUseStatistics() {
        CustomUserDetails student = principal(1L, "STUDENT");

        assertThrows(AccessDeniedException.class, () -> service.overview(student, null));
        verifyNoInteractions(stats);
    }

    @Test
    void adminAskingForAnUnknownDepartmentGetsNotFound() {
        when(stats.departmentName(99L)).thenReturn(Optional.empty());
        CustomUserDetails admin = principal(99L, "ADMIN");

        assertThrows(com.edunexus.backend.exception.ResourceNotFoundException.class,
                () -> service.overview(admin, 99L));
    }

    // ---------- overview ----------

    @Test
    void overviewIncludesCoursesWithNoStudents() {
        Department ece = withId(new Department("ECE", "Electronics"), 6L);
        Course other = withId(new Course("BTECH-ECE", "B.Tech ECE", ece, 8), 24L);
        when(stats.countStudents()).thenReturn(4L);
        when(stats.countDocuments()).thenReturn(2L);
        when(stats.studentsPerCourse()).thenReturn(List.<Object[]>of(new Object[] {23L, 4L}));
        when(courses.findAll()).thenReturn(List.of(other, course));

        OverviewResponse o = service.overview(principal(99L, "ADMIN"), null);

        assertEquals("All departments", o.departmentName());
        assertEquals(4L, o.totalStudents());
        assertEquals(2L, o.totalDocuments());
        assertEquals(2, o.studentsPerCourse().size());
        assertEquals("B.Tech CSE", o.studentsPerCourse().get(0).courseName());
        assertEquals(4L, o.studentsPerCourse().get(0).students());
        assertEquals(0L, o.studentsPerCourse().get(1).students());
    }

    // ---------- average marks ----------

    @Test
    void averageMarksPerSubjectWithPassRate() {
        StudentProfile a = student(1, "21CS001", "Alice");
        StudentProfile b = student(2, "21CS002", "Bob");
        StudentProfile c = student(3, "21CS003", "Cara");
        when(stats.findAllWithGraph()).thenReturn(List.of(
                rec(a, sub1, "90"), rec(b, sub1, "60"), rec(c, sub1, "30")));

        List<SubjectAverageResponse> r = service.averageMarks(principal(99L, "ADMIN"), null, null, null);

        assertEquals(1, r.size());
        assertEquals("S1", r.get(0).subjectCode());
        assertEquals(3L, r.get(0).students());
        assertDecimal("60.00", r.get(0).averagePercentage());
        assertDecimal("66.67", r.get(0).passRate());
    }

    @Test
    void averageMarksCanBeLimitedToOneSemester() {
        StudentProfile a = student(1, "21CS001", "Alice");
        when(stats.findAllWithGraph()).thenReturn(List.of(rec(a, sub1, "90")));

        assertTrue(service.averageMarks(principal(99L, "ADMIN"), null, null, 2).isEmpty());
    }

    // ---------- semester performance ----------

    @Test
    void semesterPerformanceAveragesSgpaPerSemester() {
        StudentProfile a = student(1, "21CS001", "Alice");
        StudentProfile b = student(2, "21CS002", "Bob");
        when(stats.findAllWithGraph()).thenReturn(List.of(
                rec(a, sub1, "95"), rec(b, sub1, "30"), rec(a, sub2, "55")));

        List<SemesterPerformanceResponse> r =
                service.semesterPerformance(principal(99L, "ADMIN"), null, null);

        assertEquals(2, r.size());
        assertEquals(1, r.get(0).semester());
        assertEquals(2L, r.get(0).students());
        assertDecimal("5.00", r.get(0).averageSgpa());
        assertDecimal("50.00", r.get(0).passRate());
        assertEquals(2, r.get(1).semester());
        assertEquals(1L, r.get(1).students());
        assertDecimal("6.00", r.get(1).averageSgpa());
        assertDecimal("100.00", r.get(1).passRate());
    }

    // ---------- departments ----------

    @Test
    void departmentStatsAreAdminOnly() {
        CustomUserDetails hod = principal(7L, "HOD");
        assertThrows(AccessDeniedException.class, () -> service.departments(hod));
        verifyNoInteractions(stats);
    }

    @Test
    void departmentStatsSummariseEachDepartment() {
        StudentProfile a = student(1, "21CS001", "Alice");
        StudentProfile b = student(2, "21CS002", "Bob");
        when(courses.findAll()).thenReturn(List.of(course));
        when(stats.studentsPerDepartment()).thenReturn(List.<Object[]>of(new Object[] {5L, 4L}));
        when(stats.findAllWithGraph()).thenReturn(List.of(rec(a, sub1, "95"), rec(b, sub1, "30")));

        List<DepartmentStatsResponse> r = service.departments(principal(99L, "ADMIN"));

        assertEquals(1, r.size());
        assertEquals("Computer Science", r.get(0).departmentName());
        assertEquals(4L, r.get(0).students());
        assertEquals(2L, r.get(0).studentsWithMarks());
        assertDecimal("5.00", r.get(0).averageCgpa());
        assertDecimal("50.00", r.get(0).passRate());
    }
}