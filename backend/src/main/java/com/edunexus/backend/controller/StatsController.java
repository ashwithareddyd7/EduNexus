package com.edunexus.backend.controller;

import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.edunexus.backend.dto.*;
import com.edunexus.backend.security.CustomUserDetails;
import com.edunexus.backend.service.StatsService;

/**
 * Dashboard statistics. A HOD always sees only their own department; an admin may pass
 * departmentId to narrow the numbers, or leave it out to see everything.
 */
@RestController
@RequestMapping("/api")
public class StatsController {

    private final StatsService service;

    public StatsController(StatsService service) { this.service = service; }

    @GetMapping("/hod/stats/overview")
    @PreAuthorize("hasAnyRole('HOD','ADMIN')")
    public OverviewResponse overview(@AuthenticationPrincipal CustomUserDetails me,
                                     @RequestParam(required = false) Long departmentId) {
        return service.overview(me, departmentId);
    }

    @GetMapping("/hod/stats/toppers")
    @PreAuthorize("hasAnyRole('HOD','ADMIN')")
    public List<TopperResponse> toppers(@AuthenticationPrincipal CustomUserDetails me,
                                        @RequestParam(required = false) Long departmentId,
                                        @RequestParam(required = false) Long courseId,
                                        @RequestParam(required = false) Integer semester,
                                        @RequestParam(defaultValue = "10") int limit) {
        return service.toppers(me, departmentId, courseId, semester, limit);
    }

    @GetMapping("/hod/stats/average-marks")
    @PreAuthorize("hasAnyRole('HOD','ADMIN')")
    public List<SubjectAverageResponse> averageMarks(@AuthenticationPrincipal CustomUserDetails me,
                                                     @RequestParam(required = false) Long departmentId,
                                                     @RequestParam(required = false) Long courseId,
                                                     @RequestParam(required = false) Integer semester) {
        return service.averageMarks(me, departmentId, courseId, semester);
    }

    @GetMapping("/hod/stats/semester-performance")
    @PreAuthorize("hasAnyRole('HOD','ADMIN')")
    public List<SemesterPerformanceResponse> semesterPerformance(
            @AuthenticationPrincipal CustomUserDetails me,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long courseId) {
        return service.semesterPerformance(me, departmentId, courseId);
    }

    @GetMapping("/admin/stats/departments")
    @PreAuthorize("hasRole('ADMIN')")
    public List<DepartmentStatsResponse> departments(@AuthenticationPrincipal CustomUserDetails me) {
        return service.departments(me);
    }
}