package com.edunexus.backend.repository;

import com.edunexus.backend.entity.StudentProfile;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentProfileRepository extends JpaRepository<StudentProfile, Long> {

    Optional<StudentProfile> findByUserId(Long userId);

    Optional<StudentProfile> findByStudentId(String studentId);

    boolean existsByStudentId(String studentId);
}