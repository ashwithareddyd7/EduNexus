package com.edunexus.backend.repository;

import com.edunexus.backend.entity.AcademicRecord;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

/** Read-only queries for the HOD/admin dashboards. Grades are computed in the service, never in SQL. */
public interface StatsRepository extends Repository<AcademicRecord, Long> {

    String GRAPH = "select r from AcademicRecord r join fetch r.student st join fetch st.course c "
            + "join fetch c.department d join fetch r.subject sub join fetch sub.semester sem";

    @Query(GRAPH)
    List<AcademicRecord> findAllWithGraph();

    @Query(GRAPH + " where d.id = :departmentId")
    List<AcademicRecord> findByDepartmentWithGraph(@Param("departmentId") Long departmentId);

    @Query(GRAPH + " where c.id = :courseId")
    List<AcademicRecord> findByCourseWithGraph(@Param("courseId") Long courseId);

    @Query("select count(s) from StudentProfile s")
    long countStudents();

    @Query("select count(s) from StudentProfile s where s.course.department.id = :departmentId")
    long countStudentsByDepartment(@Param("departmentId") Long departmentId);

    @Query("select count(d) from Document d")
    long countDocuments();

    @Query("select count(d) from Document d where d.student.course.department.id = :departmentId")
    long countDocumentsByDepartment(@Param("departmentId") Long departmentId);

    /** Rows of [courseId, studentCount]; courses without students are filled in by the service. */
    @Query("select c.id, count(s) from StudentProfile s join s.course c group by c.id")
    List<Object[]> studentsPerCourse();

    /** Rows of [departmentId, studentCount]. */
    @Query("select c.department.id, count(s) from StudentProfile s join s.course c group by c.department.id")
    List<Object[]> studentsPerDepartment();

    @Query("select d.name from Department d where d.id = :id")
    Optional<String> departmentName(@Param("id") Long id);
}