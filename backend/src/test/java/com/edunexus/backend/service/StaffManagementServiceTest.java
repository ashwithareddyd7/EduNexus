package com.edunexus.backend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import com.edunexus.backend.dto.StudentStatusResponse;
import com.edunexus.backend.entity.Document;
import com.edunexus.backend.entity.StudentProfile;
import com.edunexus.backend.entity.User;
import com.edunexus.backend.exception.BadRequestException;
import com.edunexus.backend.exception.ResourceNotFoundException;
import com.edunexus.backend.repository.DocumentAdminRepository;
import com.edunexus.backend.repository.StaffProfileRepository;
import com.edunexus.backend.repository.StatsRepository;
import com.edunexus.backend.repository.StudentProfileRepository;
import com.edunexus.backend.security.CustomUserDetails;

@ExtendWith(MockitoExtension.class)
class StaffManagementServiceTest {

    @Mock StudentProfileRepository profiles;
    @Mock DocumentAdminRepository documents;
    @Mock StatsRepository stats;
    @Mock StaffProfileRepository staff;

    StaffManagementService service;

    @BeforeEach
    void setUp() {
        service = new StaffManagementService(profiles, documents, new StaffScope(staff, stats));
    }

    private static CustomUserDetails principal(long id, String role) {
        return new CustomUserDetails(id, "x@test.com", "hash", role, true);
    }

    // ---------- student status ----------

    @Test
    void adminCanDisableAStudentAccount() {
        User u = mock(User.class);
        StudentProfile p = mock(StudentProfile.class);
        when(profiles.findById(3L)).thenReturn(Optional.of(p));
        when(p.getUser()).thenReturn(u);
        when(u.isEnabled()).thenReturn(false);

        StudentStatusResponse r = service.setStudentStatus(principal(99L, "ADMIN"), 3L, false);

        verify(u).setEnabled(false);
        assertFalse(r.enabled());
    }

    @Test
    void hodCannotChangeAccountStatus() {
        CustomUserDetails hod = principal(7L, "HOD");

        assertThrows(AccessDeniedException.class, () -> service.setStudentStatus(hod, 3L, true));
        verifyNoInteractions(profiles);
    }

    @Test
    void unknownStudentIsNotFound() {
        when(profiles.findById(3L)).thenReturn(Optional.empty());
        CustomUserDetails admin = principal(99L, "ADMIN");

        assertThrows(ResourceNotFoundException.class, () -> service.setStudentStatus(admin, 3L, true));
    }

    // ---------- document listing ----------

    @Test
    void unknownDocumentTypeIsABadRequest() {
        CustomUserDetails admin = principal(99L, "ADMIN");

        assertThrows(BadRequestException.class, () -> service.documents(admin, null, "NOPE", 0, 20));
        verifyNoInteractions(documents);
    }

    @Test
    void pageSizeIsCapped() {
        when(documents.findBy(any(Pageable.class))).thenReturn(new PageImpl<Document>(List.of()));

        service.documents(principal(99L, "ADMIN"), null, null, 0, 500);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(documents).findBy(captor.capture());
        assertEquals(50, captor.getValue().getPageSize());
    }

    @Test
    void studentsCannotListDocumentsThroughTheStaffEndpoint() {
        CustomUserDetails student = principal(1L, "STUDENT");

        assertThrows(AccessDeniedException.class, () -> service.documents(student, null, null, 0, 20));
        verifyNoInteractions(documents);
    }
}