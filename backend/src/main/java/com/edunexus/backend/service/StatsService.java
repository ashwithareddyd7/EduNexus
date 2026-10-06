package com.edunexus.backend.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.edunexus.backend.dto.*;
import com.edunexus.backend.entity.*;
import com.edunexus.backend.exception.BadRequestException;
import com.edunexus.backend.exception.ResourceNotFoundException;
import com.edunexus.backend.grading.GpaCalculator;
import com.edunexus.backend.grading.GradeScale;
import com.edunexus.backend.repository.CourseRepository;
import com.edunexus.backend.repository.StatsRepository;
import com.edunexus.backend.security.CustomUserDetails;

/**
 * Dashboard statistics. Marks are loaded once for the allowed scope and graded in Java with
 * GradeScale/GpaCalculator, so the pass mark and grade points live in exactly one place.
 * Students with no marks are left out of averages and rankings (never counted as zero).
 */
@Service
public class StatsService {

    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2);
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
    private static final int MAX_LIMIT = 50;

    private final StatsRepository stats;
    private final CourseRepository courses;
    private final StudentProfileService studentService;
    private final StaffScope scope;

    public StatsService(StatsRepository stats, CourseRepository courses,
                        StudentProfileService studentService, StaffScope scope) {
        this.stats = stats;
        this.courses = courses;
        this.studentService = studentService;
        this.scope = scope;
    }

    // ---------- overview ----------

    @Transactional(readOnly = true)
    public OverviewResponse overview(CustomUserDetails me, Long departmentId) {
        Long dept = scope.resolveDepartment(me, departmentId);
        long students = dept == null ? stats.countStudents() : stats.countStudentsByDepartment(dept);
        long documents = dept == null ? stats.countDocuments() : stats.countDocumentsByDepartment(dept);

        Map<Long, Long> perCourse = toCountMap(stats.studentsPerCourse());
        List<CourseCount> list = courses.findAll().stream()
                .filter(c -> dept == null || c.getDepartment().getId().equals(dept))
                .sorted(Comparator.comparing(Course::getName))
                .map(c -> new CourseCount(c.getId(), c.getName(), perCourse.getOrDefault(c.getId(), 0L)))
                .toList();

        String name = dept == null ? "All departments" : stats.departmentName(dept).orElse("Unknown");
        return new OverviewResponse(dept, name, students, documents, list);
    }

    // ---------- departments (admin only) ----------

    @Transactional(readOnly = true)
    public List<DepartmentStatsResponse> departments(CustomUserDetails me) {
        if (!"ADMIN".equals(me.getRole())) throw new AccessDeniedException("Not allowed");

        Map<Long, String> names = new LinkedHashMap<>();
        for (Course c : courses.findAll())
            names.putIfAbsent(c.getDepartment().getId(), c.getDepartment().getName());

        Map<Long, Long> counts = toCountMap(stats.studentsPerDepartment());
        Map<Long, List<Agg>> byDept = new HashMap<>();
        for (Agg a : aggregate(stats.findAllWithGraph(), null).values())
            byDept.computeIfAbsent(a.student.getCourse().getDepartment().getId(),
                    k -> new ArrayList<>()).add(a);

        return names.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .map(e -> {
                    List<Agg> list = byDept.getOrDefault(e.getKey(), List.of());
                    BigDecimal sum = BigDecimal.ZERO;
                    long clean = 0;
                    for (Agg a : list) {
                        sum = sum.add(GpaCalculator.gpa(a.entries));
                        if (a.failed == 0) clean++;
                    }
                    return new DepartmentStatsResponse(e.getKey(), e.getValue(),
                            counts.getOrDefault(e.getKey(), 0L), list.size(),
                            avg(sum, list.size()), rate(clean, list.size()));
                })
                .toList();
    }

    // ---------- toppers ----------

    @Transactional(readOnly = true)
    public List<TopperResponse> toppers(CustomUserDetails me, Long departmentId, Long courseId,
                                        Integer semester, int limit) {
        checkSemester(semester);
        int max = Math.max(1, Math.min(limit, MAX_LIMIT));
        List<AcademicRecord> records = loadRecords(me, departmentId, courseId);

        List<Ranked> ranked = aggregate(records, semester).values().stream()
                .map(a -> new Ranked(a, GpaCalculator.gpa(a.entries)))
                .sorted(Comparator.comparing((Ranked r) -> r.gpa(), Comparator.reverseOrder())
                        .thenComparingInt((Ranked r) -> r.agg().failed)
                        .thenComparing((Ranked r) -> r.agg().student.getStudentId()))
                .toList();

        List<TopperResponse> out = new ArrayList<>();
        int rank = 0;
        BigDecimal prev = null;
        for (int i = 0; i < ranked.size() && out.size() < max; i++) {
            Ranked r = ranked.get(i);
            if (prev == null || r.gpa().compareTo(prev) != 0) rank = i + 1;
            prev = r.gpa();
            StudentProfile s = r.agg().student;
            out.add(new TopperResponse(rank, s.getId(), s.getStudentId(), s.getFullName(),
                    s.getCourse().getName(), r.gpa(), r.agg().failed));
        }
        return out;
    }

    // ---------- average marks per subject ----------

    @Transactional(readOnly = true)
    public List<SubjectAverageResponse> averageMarks(CustomUserDetails me, Long departmentId,
                                                     Long courseId, Integer semester) {
        checkSemester(semester);
        Map<Long, SubjectAgg> map = new LinkedHashMap<>();
        for (AcademicRecord r : loadRecords(me, departmentId, courseId)) {
            Subject s = r.getSubject();
            if (semester != null && s.getSemester().getSemesterNumber() != semester) continue;
            SubjectAgg a = map.computeIfAbsent(s.getId(), k -> new SubjectAgg(s));
            a.count++;
            a.percentSum = a.percentSum.add(GradeScale.percentage(r.getMarksObtained(), s.getMaxMarks()));
            if (GradeScale.isPass(r.getMarksObtained(), s.getMaxMarks())) a.passed++;
        }
        return map.values().stream()
                .sorted(Comparator.comparingInt((SubjectAgg a) -> a.subject.getSemester().getSemesterNumber())
                        .thenComparing((SubjectAgg a) -> a.subject.getCode()))
                .map(a -> new SubjectAverageResponse(a.subject.getCode(), a.subject.getName(),
                        a.subject.getSemester().getSemesterNumber(), a.count,
                        avg(a.percentSum, a.count), rate(a.passed, a.count)))
                .toList();
    }

    // ---------- semester performance ----------

    @Transactional(readOnly = true)
    public List<SemesterPerformanceResponse> semesterPerformance(CustomUserDetails me,
                                                                 Long departmentId, Long courseId) {
        Map<Integer, Map<Long, Agg>> bySemester = new TreeMap<>();
        for (AcademicRecord r : loadRecords(me, departmentId, courseId)) {
            Subject s = r.getSubject();
            int sem = s.getSemester().getSemesterNumber();
            Agg a = bySemester.computeIfAbsent(sem, k -> new HashMap<>())
                    .computeIfAbsent(r.getStudent().getId(), k -> new Agg(r.getStudent()));
            a.add(r.getMarksObtained(), s);
        }

        List<SemesterPerformanceResponse> out = new ArrayList<>();
        for (Map.Entry<Integer, Map<Long, Agg>> e : bySemester.entrySet()) {
            BigDecimal sum = BigDecimal.ZERO;
            long clean = 0;
            for (Agg a : e.getValue().values()) {
                sum = sum.add(GpaCalculator.gpa(a.entries));
                if (a.failed == 0) clean++;
            }
            long n = e.getValue().size();
            out.add(new SemesterPerformanceResponse(e.getKey(), n, avg(sum, n), rate(clean, n)));
        }
        return out;
    }

    // ---------- helpers ----------

    private List<AcademicRecord> loadRecords(CustomUserDetails me, Long departmentId, Long courseId) {
        Long dept = scope.resolveDepartment(me, departmentId);
        if (courseId != null) {
            Course course = courses.findById(courseId)
                    .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
            Long courseDept = course.getDepartment().getId();
            studentService.assertCanManage(me, courseDept);
            if (dept != null && !dept.equals(courseDept))
                throw new BadRequestException("Course does not belong to that department");
            return stats.findByCourseWithGraph(courseId);
        }
        return dept == null ? stats.findAllWithGraph() : stats.findByDepartmentWithGraph(dept);
    }

    /** One Agg per student, over the given semester only, or over all semesters when null. */
    private Map<Long, Agg> aggregate(List<AcademicRecord> records, Integer semester) {
        Map<Long, Agg> map = new LinkedHashMap<>();
        for (AcademicRecord r : records) {
            Subject s = r.getSubject();
            if (semester != null && s.getSemester().getSemesterNumber() != semester) continue;
            map.computeIfAbsent(r.getStudent().getId(), k -> new Agg(r.getStudent()))
                    .add(r.getMarksObtained(), s);
        }
        return map;
    }

    private static Map<Long, Long> toCountMap(List<Object[]> rows) {
        Map<Long, Long> map = new HashMap<>();
        for (Object[] row : rows)
            map.put(((Number) row[0]).longValue(), ((Number) row[1]).longValue());
        return map;
    }

    private static void checkSemester(Integer semester) {
        if (semester != null && (semester < 1 || semester > 12))
            throw new BadRequestException("Semester must be between 1 and 12");
    }

    private static BigDecimal avg(BigDecimal sum, long n) {
        return n == 0 ? ZERO : sum.divide(BigDecimal.valueOf(n), 2, RoundingMode.HALF_UP);
    }

    private static BigDecimal rate(long part, long total) {
        return total == 0 ? ZERO
                : BigDecimal.valueOf(part).multiply(HUNDRED)
                        .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);
    }

    private static final class Agg {
        final StudentProfile student;
        final List<GpaCalculator.Entry> entries = new ArrayList<>();
        int failed;

        Agg(StudentProfile student) { this.student = student; }

        void add(BigDecimal marks, Subject s) {
            entries.add(new GpaCalculator.Entry(s.getCredits(),
                    GradeScale.pointsFor(marks, s.getMaxMarks())));
            if (!GradeScale.isPass(marks, s.getMaxMarks())) failed++;
        }
    }

    private static final class SubjectAgg {
        final Subject subject;
        long count;
        long passed;
        BigDecimal percentSum = BigDecimal.ZERO;

        SubjectAgg(Subject subject) { this.subject = subject; }
    }

    private record Ranked(Agg agg, BigDecimal gpa) {}
}