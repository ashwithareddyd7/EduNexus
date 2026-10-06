package com.edunexus.backend.controller;

import java.util.List;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.edunexus.backend.dto.*;
import com.edunexus.backend.security.CustomUserDetails;
import com.edunexus.backend.service.AcademicRecordService;

@RestController
@RequestMapping("/api/subjects")
public class SubjectController {

    private final AcademicRecordService service;

    public SubjectController(AcademicRecordService service) { this.service = service; }

    @PostMapping
    @PreAuthorize("hasAnyRole('HOD','ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public SubjectResponse create(@AuthenticationPrincipal CustomUserDetails me,
                                  @Valid @RequestBody CreateSubjectRequest req) {
        return service.createSubject(me, req);
    }

    @GetMapping
    public List<SubjectResponse> list(@RequestParam Long courseId,
                                      @RequestParam(required = false) Integer semester) {
        return service.listSubjects(courseId, semester);
    }
}