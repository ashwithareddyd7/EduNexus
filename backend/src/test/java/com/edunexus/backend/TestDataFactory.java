package com.edunexus.backend;

import com.edunexus.backend.entity.*;
import com.edunexus.backend.repository.*;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** Test helper: creates users, profiles, documents and JWTs. Every call uses unique names, so no cleanup is needed. */
@Component
public class TestDataFactory {

    public static final String PASSWORD = "Test@1234";

    public record StudentData(User user, StudentProfile profile, String token) {}
    public record StaffData(User user, String token) {}

    @Autowired UserRepository users;
    @Autowired RoleRepository roles;
    @Autowired DepartmentRepository departments;
    @Autowired CourseRepository courses;
    @Autowired SemesterRepository semesters;
    @Autowired SubjectRepository subjects;
    @Autowired StudentProfileRepository students;
    @Autowired StaffProfileRepository staff;
    @Autowired DocumentRepository documents;
    @Autowired AcademicRecordRepository records;
    @Autowired PasswordEncoder encoder;

    @Value("${app.jwt.secret}") String secret;

    private static String uid() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    private SecretKey key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }

    // ---------- tokens ----------

    public String tokenFor(String email) {
        Date now = new Date();
        return Jwts.builder().subject(email).issuedAt(now)
                .expiration(new Date(now.getTime() + 3_600_000)).signWith(key()).compact();
    }

    public String expiredTokenFor(String email) {
        long now = System.currentTimeMillis();
        return Jwts.builder().subject(email).issuedAt(new Date(now - 7_200_000))
                .expiration(new Date(now - 3_600_000)).signWith(key()).compact();
    }

    public String tokenSignedWithWrongKey(String email) {
        SecretKey other = Keys.hmacShaKeyFor(
                "a-completely-different-secret-key-0123456789".getBytes(StandardCharsets.UTF_8));
        Date now = new Date();
        return Jwts.builder().subject(email).issuedAt(now)
                .expiration(new Date(now.getTime() + 3_600_000)).signWith(other).compact();
    }

    // ---------- structure ----------

    public Department newDepartment() {
        String id = uid();
        return departments.save(new Department("D" + id, "Dept " + id));
    }

    public Course newCourse(Department department) {
        String id = uid();
        return courses.save(new Course("C" + id, "Course " + id, department, 8));
    }

    public Subject newSubject(Course course) {
        Semester sem = semesters.findByCourseIdAndSemesterNumber(course.getId(), 1)
                .orElseGet(() -> semesters.save(new Semester(course, 1)));
        return subjects.save(new Subject("S" + uid(), "Subject", 4, 100, sem));
    }

    // ---------- people ----------

    private User newUser(String prefix, RoleName roleName) {
        Role role = roles.findByName(roleName).orElseGet(() -> roles.save(new Role(roleName)));
        String email = prefix + "." + uid() + "@test.com";
        return users.save(new User(email, encoder.encode(PASSWORD), role));
    }

    public StudentData newStudent(Course course) {
        User user = newUser("student", RoleName.STUDENT);
        StudentProfile profile = students.save(
                new StudentProfile(user, "R" + uid(), "Test Student", course, 1));
        return new StudentData(user, profile, tokenFor(user.getEmail()));
    }

    public StaffData newHod(Department department) {
        User user = newUser("hod", RoleName.HOD);
        staff.save(new StaffProfile(user, "Test HOD", department));
        return new StaffData(user, tokenFor(user.getEmail()));
    }

    public StaffData newAdmin() {
        User user = newUser("admin", RoleName.ADMIN);
        staff.save(new StaffProfile(user, "Test Admin", null));
        return new StaffData(user, tokenFor(user.getEmail()));
    }

    // ---------- student data ----------

    public Document newDocument(StudentProfile student) {
        return documents.save(new Document(student, DocumentType.CERTIFICATE, "cert.pdf",
                UUID.randomUUID() + ".pdf", "application/pdf", 10L, "test"));
    }

    public AcademicRecord newRecord(StudentProfile student, Subject subject) {
        return records.save(new AcademicRecord(student, subject, new BigDecimal("80.00")));
    }
}
