package com.edunexus.backend.controller;

import com.edunexus.backend.dto.CourseResponse;
import com.edunexus.backend.repository.CourseRepository;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseRepository courses;

    public CourseController(CourseRepository courses) {
        this.courses = courses;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public List<CourseResponse> list() {
        return courses.findAll().stream()
                .map(c -> new CourseResponse(
                        c.getId(),
                        c.getCode(),
                        c.getName(),
                        c.getDepartment().getName(),
                        c.getTotalSemesters()))
                .toList();
    }
}