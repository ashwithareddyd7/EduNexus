package com.edunexus.backend.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.core.io.Resource;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.edunexus.backend.dto.DocumentResponse;
import com.edunexus.backend.entity.Document;
import com.edunexus.backend.entity.DocumentType;
import com.edunexus.backend.entity.StudentProfile;
import com.edunexus.backend.exception.BadRequestException;
import com.edunexus.backend.exception.ResourceNotFoundException;
import com.edunexus.backend.repository.DocumentRepository;
import com.edunexus.backend.repository.StudentProfileRepository;
import com.edunexus.backend.security.CustomUserDetails;
import com.edunexus.backend.upload.FileSignature;

@Service
public class DocumentService {

    static final long MAX_BYTES = 5L * 1024 * 1024;

    private final DocumentRepository documents;
    private final StudentProfileRepository profiles;
    private final StudentProfileService studentService;
    private final FileStorageService storage;

    public DocumentService(DocumentRepository documents, StudentProfileRepository profiles,
                           StudentProfileService studentService, FileStorageService storage) {
        this.documents = documents; this.profiles = profiles;
        this.studentService = studentService; this.storage = storage;
    }

    public record DownloadedFile(String filename, String contentType, long size, Resource resource) {}

    @Transactional
    public DocumentResponse upload(Long userId, String type, String description, MultipartFile file) {
        DocumentType docType = parseType(type);
        if (description != null && description.length() > 255)
            throw new BadRequestException("Description is too long");
        if (file == null || file.isEmpty())
            throw new BadRequestException("File is required");
        if (file.getSize() > MAX_BYTES)
            throw new BadRequestException("File exceeds the 5 MB limit");

        FileSignature.Type detected = detect(file);
        StudentProfile student = profiles.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Create your profile first"));

        String storedName = UUID.randomUUID() + "." + detected.extension();
        try {
            storage.store(storedName, file);
        } catch (IOException e) {
            throw new IllegalStateException("Could not store the file", e);
        }
        try {
            Document saved = documents.save(new Document(student, docType,
                    cleanName(file.getOriginalFilename()), storedName,
                    detected.contentType(), file.getSize(), blankToNull(description)));
            return toResponse(saved);
        } catch (RuntimeException e) {
            storage.delete(storedName);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public List<DocumentResponse> listMine(Long userId) {
        StudentProfile student = profiles.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));
        return documents.findByStudentId(student.getId()).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<DocumentResponse> listForStudent(Long studentId, CustomUserDetails me) {
        StudentProfile student = profiles.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        studentService.assertCanView(me, student);
        return documents.findByStudentId(student.getId()).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public DownloadedFile download(Long documentId, CustomUserDetails me) {
        Document d = documents.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found"));
        studentService.assertCanView(me, d.getStudent());
        return new DownloadedFile(d.getOriginalFilename(), d.getContentType(),
                d.getSizeBytes(), storage.load(d.getStoredFilename()));
    }

    @Transactional
    public void delete(Long documentId, CustomUserDetails me) {
        Document d = documents.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found"));
        boolean allowed = "ADMIN".equals(me.getRole())
                || ("STUDENT".equals(me.getRole())
                    && d.getStudent().getUser().getId().equals(me.getId()));
        if (!allowed) throw new AccessDeniedException("Not allowed");
        String stored = d.getStoredFilename();
        documents.delete(d);
        storage.delete(stored);
    }

    private FileSignature.Type detect(MultipartFile file) {
        try (InputStream in = file.getInputStream()) {
            byte[] header = in.readNBytes(8);
            return FileSignature.detect(header).orElseThrow(
                    () -> new BadRequestException("Only PDF, JPG and PNG files are allowed"));
        } catch (IOException e) {
            throw new BadRequestException("Could not read the file");
        }
    }

    private static DocumentType parseType(String type) {
        try {
            return DocumentType.valueOf(type.trim().toUpperCase(Locale.ROOT));
        } catch (RuntimeException e) {
            throw new BadRequestException("Unknown document type");
        }
    }

    /** The client's filename is display metadata only; strip any path and control characters. */
    private static String cleanName(String name) {
        if (name == null) return "file";
        String n = name.replace('\\', '/');
        n = n.substring(n.lastIndexOf('/') + 1).replaceAll("\\p{Cntrl}", "").trim();
        if (n.isEmpty()) return "file";
        return n.length() > 255 ? n.substring(n.length() - 255) : n;
    }

    private static String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }

    private DocumentResponse toResponse(Document d) {
        return new DocumentResponse(d.getId(), d.getDocumentType(), d.getOriginalFilename(),
                d.getContentType(), d.getSizeBytes(), d.getDescription());
    }
}