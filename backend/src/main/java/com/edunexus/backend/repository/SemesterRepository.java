package com.edunexus.backend.repository;

import com.edunexus.backend.entity.Semester;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SemesterRepository extends JpaRepository<Semester, Long> {

    List<Semester> findByCourseIdOrderBySemesterNumber(Long courseId);

    Optional<Semester> findByCourseIdAndSemesterNumber(Long courseId, int semesterNumber);
}