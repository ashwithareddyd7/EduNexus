package com.edunexus.backend.service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import com.edunexus.backend.entity.StaffProfile;
import com.edunexus.backend.exception.ResourceNotFoundException;
import com.edunexus.backend.repository.StaffProfileRepository;
import com.edunexus.backend.repository.StatsRepository;
import com.edunexus.backend.security.CustomUserDetails;

/**
 * Decides which department a staff member may look at.
 * ADMIN: any department, or null meaning all of them. HOD: only their own department, always.
 * Anyone else is refused.
 */
@Component
public class StaffScope {

    private final StaffProfileRepository staff;
    private final StatsRepository stats;

    public StaffScope(StaffProfileRepository staff, StatsRepository stats) {
        this.staff = staff;
        this.stats = stats;
    }

    public Long resolveDepartment(CustomUserDetails me, Long requestedDepartmentId) {
        String role = me.getRole();
        if ("ADMIN".equals(role)) {
            if (requestedDepartmentId != null && stats.departmentName(requestedDepartmentId).isEmpty())
                throw new ResourceNotFoundException("Department not found");
            return requestedDepartmentId;
        }
        if ("HOD".equals(role)) {
            StaffProfile s = staff.findByUserId(me.getId())
                    .orElseThrow(() -> new AccessDeniedException("No staff profile found"));
            if (s.getDepartment() == null)
                throw new AccessDeniedException("No department assigned");
            Long own = s.getDepartment().getId();
            if (requestedDepartmentId != null && !requestedDepartmentId.equals(own))
                throw new AccessDeniedException("You do not have permission to view that department");
            return own;
        }
        throw new AccessDeniedException("Not allowed");
    }
}