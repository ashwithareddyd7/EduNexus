package com.edunexus.backend.controller;

import java.util.List;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.edunexus.backend.dto.*;
import com.edunexus.backend.security.CustomUserDetails;
import com.edunexus.backend.service.AcademicRecordService;

@RestController
@RequestMapping("/api")
public class AcademicRecordController {

    private final AcademicRecordService service;

    public AcademicRecordController(AcademicRecordService service) { this.service = service; }

    @PutMapping("/records")
    @PreAuthorize("hasAnyRole('HOD','ADMIN')")
    public RecordResponse saveMarks(@AuthenticationPrincipal CustomUserDetails me,
                                    @Valid @RequestBody EnterMarksRequest req) {
        return service.saveMarks(me, req);
    }

    @GetMapping("/students/{id}/records")
    @PreAuthorize("hasAnyRole('STUDENT','HOD','ADMIN')")
    public List<RecordResponse> records(@PathVariable Long id,
                                        @AuthenticationPrincipal CustomUserDetails me) {
        return service.listRecords(id, me);
    }

    @GetMapping("/students/{id}/gpa")
    @PreAuthorize("hasAnyRole('STUDENT','HOD','ADMIN')")
    public GpaResponse gpa(@PathVariable Long id,
                           @AuthenticationPrincipal CustomUserDetails me) {
        return service.gpa(id, me);
    }
}