package com.edunexus.backend.repository;

import com.edunexus.backend.entity.AcademicRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AcademicRecordRepository extends JpaRepository<AcademicRecord, Long> {

    Optional<AcademicRecord> findByStudent_IdAndSubject_Id(
            Long studentProfileId,
            Long subjectId
    );
}