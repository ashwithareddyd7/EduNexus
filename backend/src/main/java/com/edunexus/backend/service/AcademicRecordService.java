package com.edunexus.backend.service;

import java.math.BigDecimal;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.edunexus.backend.dto.*;
import com.edunexus.backend.entity.*;
import com.edunexus.backend.exception.*;
import com.edunexus.backend.grading.*;
import com.edunexus.backend.repository.*;
import com.edunexus.backend.security.CustomUserDetails;

@Service
public class AcademicRecordService {

    private final SubjectRepository subjects;
    private final SemesterRepository semesters;
    private final AcademicRecordRepository records;
    private final StudentProfileRepository profiles;
    private final CourseRepository courses;
    private final StudentProfileService studentService;

    public AcademicRecordService(SubjectRepository subjects, SemesterRepository semesters,
                                 AcademicRecordRepository records,
                                 StudentProfileRepository profiles, CourseRepository courses,
                                 StudentProfileService studentService) {
        this.subjects = subjects; this.semesters = semesters; this.records = records;
        this.profiles = profiles; this.courses = courses; this.studentService = studentService;
    }

    @Transactional
    public SubjectResponse createSubject(CustomUserDetails me, CreateSubjectRequest req) {
        Course course = courses.findById(req.courseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
        studentService.assertCanManage(me, course.getDepartment().getId());
        if (req.semesterNumber() > course.getTotalSemesters())
            throw new BadRequestException("Semester exceeds course length");
        if (subjects.findByCode(req.code()).isPresent())
            throw new ConflictException("Subject code already in use");

        Semester semester = semesters
                .findByCourseIdAndSemesterNumber(course.getId(), req.semesterNumber())
                .orElseGet(() -> semesters.save(new Semester(course, req.semesterNumber())));
        Subject s = subjects.save(new Subject(req.code(), req.name(),
                req.credits(), req.maxMarks(), semester));
        return toSubjectResponse(s);
    }

    @Transactional(readOnly = true)
    public List<SubjectResponse> listSubjects(Long courseId, Integer semesterNumber) {
        List<Subject> out = new ArrayList<>();
        for (Semester sem : semesters.findByCourseIdOrderBySemesterNumber(courseId)) {
            if (semesterNumber != null && sem.getSemesterNumber() != semesterNumber) continue;
            out.addAll(subjects.findBySemesterId(sem.getId()));
        }
        return out.stream().map(this::toSubjectResponse).toList();
    }

    /** Creates the record, or overwrites the marks if one already exists (a retake). */
    @Transactional
    public RecordResponse saveMarks(CustomUserDetails me, EnterMarksRequest req) {
        StudentProfile student = profiles.findById(req.studentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        Subject subject = subjects.findById(req.subjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found"));
        studentService.assertCanManage(me, student.getCourse().getDepartment().getId());
        if (!subject.getSemester().getCourse().getId().equals(student.getCourse().getId()))
            throw new BadRequestException("Subject does not belong to the student's course");
        if (req.marks().compareTo(BigDecimal.valueOf(subject.getMaxMarks())) > 0)
            throw new BadRequestException("Marks exceed the subject's maximum");

        AcademicRecord rec = records.findByStudentIdAndSubjectId(student.getId(), subject.getId())
                .orElse(null);
        if (rec == null) {
            rec = records.save(new AcademicRecord(student, subject, req.marks()));
        } else {
            rec.setMarksObtained(req.marks());
        }
        return toRecordResponse(rec);
    }

    @Transactional(readOnly = true)
    public List<RecordResponse> listRecords(Long studentId, CustomUserDetails me) {
        StudentProfile student = loadViewable(studentId, me);
        return records.findByStudentId(student.getId()).stream()
                .sorted(Comparator
                        .comparingInt((AcademicRecord r) -> r.getSubject().getSemester().getSemesterNumber())
                        .thenComparing(r -> r.getSubject().getCode()))
                .map(this::toRecordResponse).toList();
    }

    @Transactional(readOnly = true)
    public GpaResponse gpa(Long studentId, CustomUserDetails me) {
        StudentProfile student = loadViewable(studentId, me);

        Map<Integer, List<GpaCalculator.Entry>> bySemester = new TreeMap<>();
        List<GpaCalculator.Entry> all = new ArrayList<>();
        for (AcademicRecord r : records.findByStudentId(student.getId())) {
            Subject s = r.getSubject();
            var e = new GpaCalculator.Entry(s.getCredits(),
                    GradeScale.pointsFor(r.getMarksObtained(), s.getMaxMarks()));
            bySemester.computeIfAbsent(s.getSemester().getSemesterNumber(),
                    k -> new ArrayList<>()).add(e);
            all.add(e);
        }

        List<SemesterGpa> perSemester = bySemester.entrySet().stream()
                .map(en -> new SemesterGpa(en.getKey(),
                        en.getValue().stream().mapToInt(GpaCalculator.Entry::credits).sum(),
                        GpaCalculator.gpa(en.getValue())))
                .toList();
        return new GpaResponse(perSemester, GpaCalculator.gpa(all));
    }

    private StudentProfile loadViewable(Long studentId, CustomUserDetails me) {
        StudentProfile s = profiles.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        studentService.assertCanView(me, s);
        return s;
    }

    private SubjectResponse toSubjectResponse(Subject s) {
        return new SubjectResponse(s.getId(), s.getCode(), s.getName(), s.getCredits(),
                s.getMaxMarks(), s.getSemester().getSemesterNumber(),
                s.getSemester().getCourse().getId());
    }

    private RecordResponse toRecordResponse(AcademicRecord r) {
        Subject s = r.getSubject();
        BigDecimal marks = r.getMarksObtained();
        return new RecordResponse(r.getId(), s.getCode(), s.getName(),
                s.getSemester().getSemesterNumber(), s.getCredits(), s.getMaxMarks(), marks,
                GradeScale.percentage(marks, s.getMaxMarks()),
                GradeScale.letterFor(marks, s.getMaxMarks()),
                GradeScale.pointsFor(marks, s.getMaxMarks()),
                GradeScale.isPass(marks, s.getMaxMarks()));
    }
}