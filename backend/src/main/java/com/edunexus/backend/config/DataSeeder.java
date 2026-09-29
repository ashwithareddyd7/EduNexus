package com.edunexus.backend.config;

import com.edunexus.backend.entity.Course;
import com.edunexus.backend.entity.Department;
import com.edunexus.backend.repository.CourseRepository;
import com.edunexus.backend.repository.DepartmentRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final DepartmentRepository departments;
    private final CourseRepository courses;

    public DataSeeder(DepartmentRepository departments, CourseRepository courses) {
        this.departments = departments;
        this.courses = courses;
    }

    @Override
    public void run(String... args) {
        if (departments.count() > 0) return;
        Department cse = departments.save(new Department("CSE", "Computer Science and Engineering"));
        Department ece = departments.save(new Department("ECE", "Electronics and Communication Engineering"));
        courses.save(new Course("BTECH-CSE", "B.Tech Computer Science", cse, 8));
        courses.save(new Course("BTECH-ECE", "B.Tech Electronics", ece, 8));
    }
}