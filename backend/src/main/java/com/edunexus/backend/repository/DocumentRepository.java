package com.edunexus.backend.repository;

import com.edunexus.backend.entity.Document;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRepository extends JpaRepository<Document, Long> {

    List<Document> findByStudentId(Long studentProfileId);

    /** Ownership-safe lookup: returns a document only if it belongs to the given student. */
    Optional<Document> findByIdAndStudentId(Long documentId, Long studentProfileId);
}