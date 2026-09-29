package com.edunexus.backend.security;

import com.edunexus.backend.entity.StaffProfile;
import com.edunexus.backend.repository.StaffProfileRepository;
import com.edunexus.backend.repository.StudentProfileRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Object-level access rules: stops a user from reading someone else's data by changing an ID in the URL.
 * Use in controllers: @PreAuthorize("@accessPolicy.canAccessStudent(authentication, #id)")
 * Rules: ADMIN any student, HOD students in their own department, STUDENT only themselves.
 * Anything unclear returns false (403), so we never reveal whether a record exists.
 */
@Component("accessPolicy")
public class AccessPolicy {

    private final StudentProfileRepository studentRepo;
    private final StaffProfileRepository staffRepo;

    public AccessPolicy(StudentProfileRepository studentRepo, StaffProfileRepository staffRepo) {
        this.studentRepo = studentRepo;
        this.staffRepo = staffRepo;
    }

    @Transactional(readOnly = true)
    public boolean canAccessStudent(Authentication auth, Long studentProfileId) {
        if (auth == null || !auth.isAuthenticated()
                || !(auth.getPrincipal() instanceof CustomUserDetails user)
                || studentProfileId == null) {
            return false;
        }
        return switch (user.getRole()) {
            case "ADMIN" -> true;
            case "STUDENT" -> studentRepo.findByUserId(user.getId())
                    .map(own -> own.getId().equals(studentProfileId))
                    .orElse(false);
            case "HOD" -> staffRepo.findByUserId(user.getId())
                    .map(StaffProfile::getDepartment)
                    .flatMap(dept -> studentRepo.findById(studentProfileId)
                            .map(s -> s.getCourse().getDepartment().getId().equals(dept.getId())))
                    .orElse(false);
            default -> false;
        };
    }
}