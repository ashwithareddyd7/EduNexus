package com.edunexus.backend.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.edunexus.backend.repository.AcademicRecordRepository;
import com.edunexus.backend.repository.CourseRepository;
import com.edunexus.backend.repository.DepartmentRepository;
import com.edunexus.backend.repository.DocumentRepository;
import com.edunexus.backend.repository.RoleRepository;
import com.edunexus.backend.repository.SemesterRepository;
import com.edunexus.backend.repository.StudentProfileRepository;
import com.edunexus.backend.repository.SubjectRepository;
import com.edunexus.backend.repository.UserRepository;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

/**
 * Runs against the real PostgreSQL database, but every test is rolled back afterwards,
 * so no test data is left behind.
 */
@SpringBootTest
@Transactional
class EntityRelationshipsTest {

    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private DepartmentRepository departmentRepository;
    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private StudentProfileRepository studentProfileRepository;
    @Autowired
    private SemesterRepository semesterRepository;
    @Autowired
    private SubjectRepository subjectRepository;
    @Autowired
    private AcademicRecordRepository academicRecordRepository;
    @Autowired
    private DocumentRepository documentRepository;

    @Test
    void allThreeRolesAreSeeded() {
        for (RoleName name : RoleName.values()) {
            assertTrue(roleRepository.existsByName(name), "Missing role " + name);
        }
        assertEquals(3, roleRepository.count());
    }

    @Test
    void fullAcademicChainCanBeSavedAndQueried() {
        Role studentRole = roleRepository.findByName(RoleName.STUDENT).orElseThrow();
        Department dept = departmentRepository.saveAndFlush(new Department("TST", "Test Department"));
        Course course = courseRepository.saveAndFlush(new Course("TSTCRS", "Test Course", dept, 8));
        User user = userRepository.saveAndFlush(
                new User("student.test@example.com", "not-a-real-hash", studentRole));
        StudentProfile profile = studentProfileRepository.saveAndFlush(
                new StudentProfile(user, "TST001", "Test Student", course, 3));
        Semester semester = semesterRepository.saveAndFlush(new Semester(course, 3));
        Subject subject = subjectRepository.saveAndFlush(new Subject("TST301", "Test Subject", 4, 100, semester));
        AcademicRecord record = academicRecordRepository.saveAndFlush(
                new AcademicRecord(profile, subject, new BigDecimal("87.50")));
        Document document = documentRepository.saveAndFlush(new Document(
                profile, DocumentType.CERTIFICATE, "cert.pdf", "abc-123.pdf", "application/pdf", 1024L, null));

        // Auto-filled ids and timestamps
        assertNotNull(record.getId());
        assertNotNull(record.getCreatedAt());
        assertNotNull(record.getUpdatedAt());

        // Relationships can be navigated: student -> course -> department
        assertEquals("TST", profile.getCourse().getDepartment().getCode());
        assertEquals(1, academicRecordRepository.findByStudentId(profile.getId()).size());
        assertEquals(1, documentRepository.findByStudentId(profile.getId()).size());

        // Ownership-safe lookup: the right owner finds it, another student's id does not
        assertTrue(documentRepository.findByIdAndStudentId(document.getId(), profile.getId()).isPresent());
        assertTrue(documentRepository.findByIdAndStudentId(document.getId(), profile.getId() + 999).isEmpty());
    }

    @Test
    void duplicateSemesterForSameCourseIsRejectedByDatabase() {
        Department dept = departmentRepository.saveAndFlush(new Department("TST2", "Test Department 2"));
        Course course = courseRepository.saveAndFlush(new Course("TSTCRS2", "Test Course 2", dept, 8));
        semesterRepository.saveAndFlush(new Semester(course, 1));

        assertThrows(DataIntegrityViolationException.class,
                () -> semesterRepository.saveAndFlush(new Semester(course, 1)));
    }
}