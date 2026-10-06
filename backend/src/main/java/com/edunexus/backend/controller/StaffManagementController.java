package com.edunexus.backend.controller;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.edunexus.backend.dto.DocumentSummaryResponse;
import com.edunexus.backend.dto.StudentStatusResponse;
import com.edunexus.backend.dto.UpdateStatusRequest;
import com.edunexus.backend.security.CustomUserDetails;
import com.edunexus.backend.service.StaffManagementService;

@RestController
@RequestMapping("/api")
public class StaffManagementController {

    private final StaffManagementService service;

    public StaffManagementController(StaffManagementService service) { this.service = service; }

    /** Download and delete reuse the Phase 10 endpoints (/api/documents/{id}/download, DELETE /api/documents/{id}). */
    @GetMapping("/hod/documents")
    @PreAuthorize("hasAnyRole('HOD','ADMIN')")
    public Page<DocumentSummaryResponse> documents(@AuthenticationPrincipal CustomUserDetails me,
                                                   @RequestParam(required = false) Long departmentId,
                                                   @RequestParam(required = false) String type,
                                                   @RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "20") int size) {
        return service.documents(me, departmentId, type, page, size);
    }

    @PatchMapping("/admin/students/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public StudentStatusResponse setStatus(@AuthenticationPrincipal CustomUserDetails me,
                                           @PathVariable Long id,
                                           @Valid @RequestBody UpdateStatusRequest req) {
        return service.setStudentStatus(me, id, req.enabled());
    }
}