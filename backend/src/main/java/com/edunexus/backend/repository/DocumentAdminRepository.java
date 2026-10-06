package com.edunexus.backend.repository;

import com.edunexus.backend.entity.Document;
import com.edunexus.backend.entity.DocumentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.Repository;

/** Paged document listing for staff. The entity graph avoids one query per row. */
public interface DocumentAdminRepository extends Repository<Document, Long> {

    @EntityGraph(attributePaths = {"student", "student.course", "student.course.department"})
    Page<Document> findBy(Pageable pageable);

    @EntityGraph(attributePaths = {"student", "student.course", "student.course.department"})
    Page<Document> findByDocumentType(DocumentType documentType, Pageable pageable);

    @EntityGraph(attributePaths = {"student", "student.course", "student.course.department"})
    Page<Document> findByStudentCourseDepartmentId(Long departmentId, Pageable pageable);

    @EntityGraph(attributePaths = {"student", "student.course", "student.course.department"})
    Page<Document> findByStudentCourseDepartmentIdAndDocumentType(Long departmentId,
                                                                  DocumentType documentType,
                                                                  Pageable pageable);
}