package com.edunexus.backend.controller;

import com.edunexus.backend.dto.CreateStudentProfileRequest;
import com.edunexus.backend.dto.StudentProfileResponse;
import com.edunexus.backend.dto.UpdateStudentProfileRequest;
import com.edunexus.backend.security.CustomUserDetails;
import com.edunexus.backend.service.StudentProfileService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/students")
public class StudentProfileController {

    private final StudentProfileService service;

    public StudentProfileController(StudentProfileService service) {
        this.service = service;
    }

    @PostMapping("/me")
    @PreAuthorize("hasRole('STUDENT')")
    @ResponseStatus(HttpStatus.CREATED)
    public StudentProfileResponse create(@AuthenticationPrincipal CustomUserDetails me,
                                         @Valid @RequestBody CreateStudentProfileRequest req) {
        return service.createMine(me.getId(), req);
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('STUDENT')")
    public StudentProfileResponse mine(@AuthenticationPrincipal CustomUserDetails me) {
        return service.getMine(me.getId());
    }

    @PutMapping("/me")
    @PreAuthorize("hasRole('STUDENT')")
    public StudentProfileResponse update(@AuthenticationPrincipal CustomUserDetails me,
                                         @Valid @RequestBody UpdateStudentProfileRequest req) {
        return service.updateMine(me.getId(), req);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('STUDENT','HOD','ADMIN')")
    public StudentProfileResponse get(@PathVariable Long id,
                                      @AuthenticationPrincipal CustomUserDetails me) {
        return service.getById(id, me);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('HOD','ADMIN')")
    public Page<StudentProfileResponse> list(@AuthenticationPrincipal CustomUserDetails me,
                                             Pageable pageable) {
        return service.list(me, pageable);
    }
}