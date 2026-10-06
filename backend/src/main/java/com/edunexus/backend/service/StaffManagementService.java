package com.edunexus.backend.service;

import java.util.Locale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.edunexus.backend.dto.DocumentSummaryResponse;
import com.edunexus.backend.dto.StudentStatusResponse;
import com.edunexus.backend.entity.Document;
import com.edunexus.backend.entity.DocumentType;
import com.edunexus.backend.entity.StudentProfile;
import com.edunexus.backend.entity.User;
import com.edunexus.backend.exception.BadRequestException;
import com.edunexus.backend.exception.ResourceNotFoundException;
import com.edunexus.backend.repository.DocumentAdminRepository;
import com.edunexus.backend.repository.StudentProfileRepository;
import com.edunexus.backend.security.CustomUserDetails;

@Service
public class StaffManagementService {

    private static final int MAX_PAGE_SIZE = 50;

    private final StudentProfileRepository profiles;
    private final DocumentAdminRepository documents;
    private final StaffScope scope;

    public StaffManagementService(StudentProfileRepository profiles, DocumentAdminRepository documents,
                                  StaffScope scope) {
        this.profiles = profiles;
        this.documents = documents;
        this.scope = scope;
    }

    /** Admin only. Disabling blocks login and every later request; no data is deleted. */
    @Transactional
    public StudentStatusResponse setStudentStatus(CustomUserDetails me, Long studentProfileId,
                                                  boolean enabled) {
        if (!"ADMIN".equals(me.getRole())) throw new AccessDeniedException("Not allowed");
        StudentProfile p = profiles.findById(studentProfileId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        User u = p.getUser();
        u.setEnabled(enabled);
        return new StudentStatusResponse(p.getId(), p.getStudentId(), p.getFullName(),
                u.getEmail(), u.isEnabled());
    }

    @Transactional(readOnly = true)
    public Page<DocumentSummaryResponse> documents(CustomUserDetails me, Long departmentId,
                                                   String type, int page, int size) {
        Long dept = scope.resolveDepartment(me, departmentId);
        DocumentType docType = parseType(type);
        Pageable pageable = PageRequest.of(Math.max(page, 0),
                Math.max(1, Math.min(size, MAX_PAGE_SIZE)), Sort.by(Sort.Direction.DESC, "id"));

        Page<Document> result;
        if (dept == null && docType == null) result = documents.findBy(pageable);
        else if (dept == null) result = documents.findByDocumentType(docType, pageable);
        else if (docType == null) result = documents.findByStudentCourseDepartmentId(dept, pageable);
        else result = documents.findByStudentCourseDepartmentIdAndDocumentType(dept, docType, pageable);
        return result.map(this::toSummary);
    }

    private static DocumentType parseType(String type) {
        if (type == null || type.isBlank()) return null;
        try {
            return DocumentType.valueOf(type.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Unknown document type");
        }
    }

    private DocumentSummaryResponse toSummary(Document d) {
        StudentProfile s = d.getStudent();
        return new DocumentSummaryResponse(d.getId(), s.getId(), s.getStudentId(), s.getFullName(),
                s.getCourse().getName(), d.getDocumentType(), d.getOriginalFilename(),
                d.getContentType(), d.getSizeBytes(), d.getDescription());
    }
}