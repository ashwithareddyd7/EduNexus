package com.edunexus.backend.controller;

import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.edunexus.backend.dto.DocumentResponse;
import com.edunexus.backend.security.CustomUserDetails;
import com.edunexus.backend.service.DocumentService;

@RestController
@RequestMapping("/api")
public class DocumentController {

    private final DocumentService service;

    public DocumentController(DocumentService service) { this.service = service; }

    @PostMapping(path = "/students/me/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('STUDENT')")
    @ResponseStatus(HttpStatus.CREATED)
    public DocumentResponse upload(@AuthenticationPrincipal CustomUserDetails me,
                                   @RequestParam("file") MultipartFile file,
                                   @RequestParam("type") String type,
                                   @RequestParam(value = "description", required = false) String description) {
        return service.upload(me.getId(), type, description, file);
    }

    @GetMapping("/students/me/documents")
    @PreAuthorize("hasRole('STUDENT')")
    public List<DocumentResponse> mine(@AuthenticationPrincipal CustomUserDetails me) {
        return service.listMine(me.getId());
    }

    @GetMapping("/students/{id}/documents")
    @PreAuthorize("hasAnyRole('STUDENT','HOD','ADMIN')")
    public List<DocumentResponse> forStudent(@PathVariable Long id,
                                             @AuthenticationPrincipal CustomUserDetails me) {
        return service.listForStudent(id, me);
    }

    @GetMapping("/documents/{id}/download")
    @PreAuthorize("hasAnyRole('STUDENT','HOD','ADMIN')")
    public ResponseEntity<Resource> download(@PathVariable Long id,
                                             @AuthenticationPrincipal CustomUserDetails me) {
        DocumentService.DownloadedFile f = service.download(id, me);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(f.contentType()))
                .contentLength(f.size())
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(f.filename(), StandardCharsets.UTF_8).build().toString())
                .header("X-Content-Type-Options", "nosniff")
                .header(HttpHeaders.CACHE_CONTROL, "private, no-store")
                .body(f.resource());
    }

    @DeleteMapping("/documents/{id}")
    @PreAuthorize("hasAnyRole('STUDENT','ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails me) {
        service.delete(id, me);
    }
}