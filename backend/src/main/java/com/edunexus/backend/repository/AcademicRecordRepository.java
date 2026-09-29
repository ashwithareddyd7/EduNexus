package com.edunexus.backend.repository;

import com.edunexus.backend.entity.AcademicRecord;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AcademicRecordRepository extends JpaRepository<AcademicRecord, Long> {

    List<AcademicRecord> findByStudentId(Long studentProfileId);

    Optional<AcademicRecord> findByStudentIdAndSubjectId(Long studentProfileId, Long subjectId);
}