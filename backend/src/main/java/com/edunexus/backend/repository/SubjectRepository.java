package com.edunexus.backend.repository;

import com.edunexus.backend.entity.Subject;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubjectRepository extends JpaRepository<Subject, Long> {

    Optional<Subject> findByCode(String code);

    List<Subject> findBySemesterId(Long semesterId);
}