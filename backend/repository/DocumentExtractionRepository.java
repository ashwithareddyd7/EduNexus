package com.edunexus.backend.repository;

import com.edunexus.backend.entity.DocumentExtraction;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentExtractionRepository extends JpaRepository<DocumentExtraction, Long> {
    List<DocumentExtraction> findByDocumentId(Long documentId);
}