package com.edunexus.backend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.BeanUtils;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;
import com.edunexus.backend.dto.DocumentResponse;
import com.edunexus.backend.entity.Document;
import com.edunexus.backend.entity.StudentProfile;
import com.edunexus.backend.entity.User;
import com.edunexus.backend.exception.BadRequestException;
import com.edunexus.backend.repository.DocumentRepository;
import com.edunexus.backend.repository.StudentProfileRepository;
import com.edunexus.backend.security.CustomUserDetails;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

    @Mock DocumentRepository documents;
    @Mock StudentProfileRepository profiles;
    @Mock StudentProfileService studentService;
    @Mock FileStorageService storage;
    @InjectMocks DocumentService service;

    private static final byte[] PDF = "%PDF-1.4 test".getBytes(StandardCharsets.US_ASCII);

    private CustomUserDetails principal(long id, String role) {
        return new CustomUserDetails(id, "x@test.com", "hash", role, true);
    }

    private Document docOwnedBy(long ownerUserId) {
        User u = mock(User.class);
        lenient().when(u.getId()).thenReturn(ownerUserId);
        StudentProfile sp = mock(StudentProfile.class);
        lenient().when(sp.getUser()).thenReturn(u);
        Document d = BeanUtils.instantiateClass(Document.class);
        ReflectionTestUtils.setField(d, "student", sp);
        ReflectionTestUtils.setField(d, "storedFilename", "abc.pdf");
        return d;
    }

    // ---------- upload ----------

    @Test
    void rejectsFakePdfByContent() {
        MockMultipartFile f = new MockMultipartFile("file", "evil.pdf", "application/pdf",
                "not really a pdf".getBytes(StandardCharsets.US_ASCII));

        assertThrows(BadRequestException.class, () -> service.upload(1L, "MARKS_MEMO", null, f));
        verifyNoInteractions(storage);
    }

    @Test
    void rejectsOversizedFile() {
        byte[] big = new byte[5 * 1024 * 1024 + 1];
        System.arraycopy(PDF, 0, big, 0, PDF.length);
        MockMultipartFile f = new MockMultipartFile("file", "big.pdf", "application/pdf", big);

        assertThrows(BadRequestException.class, () -> service.upload(1L, "MARKS_MEMO", null, f));
        verifyNoInteractions(storage);
    }

    @Test
    void rejectsUnknownType() {
        MockMultipartFile f = new MockMultipartFile("file", "a.pdf", "application/pdf", PDF);

        assertThrows(BadRequestException.class, () -> service.upload(1L, "NOPE", null, f));
    }

    @Test
    void storesValidPdfWithRandomNameAndCleanDisplayName() throws Exception {
        StudentProfile sp = mock(StudentProfile.class);
        when(profiles.findByUserId(1L)).thenReturn(Optional.of(sp));
        when(documents.save(any(Document.class))).thenAnswer(i -> i.getArgument(0));
        MockMultipartFile f = new MockMultipartFile("file", "../../evil.pdf", "text/plain", PDF);

        DocumentResponse r = service.upload(1L, "MARKS_MEMO", "my marks", f);

        assertEquals("evil.pdf", r.originalFilename());
        assertEquals("application/pdf", r.contentType());
        ArgumentCaptor<String> name = ArgumentCaptor.forClass(String.class);
        verify(storage).store(name.capture(), eq(f));
        assertTrue(name.getValue().matches("[0-9a-f\\-]{36}\\.pdf"));
    }

    // ---------- delete ----------

    @Test
    void ownerStudentCanDelete() {
        Document d = docOwnedBy(1L);
        when(documents.findById(5L)).thenReturn(Optional.of(d));

        service.delete(5L, principal(1L, "STUDENT"));

        verify(documents).delete(d);
        verify(storage).delete("abc.pdf");
    }

    @Test
    void otherStudentCannotDelete() {
        Document d = docOwnedBy(1L);
        when(documents.findById(5L)).thenReturn(Optional.of(d));
        CustomUserDetails other = principal(2L, "STUDENT");

        assertThrows(AccessDeniedException.class, () -> service.delete(5L, other));
        verify(documents, never()).delete(any(Document.class));
    }

    @Test
    void hodCannotDelete() {
        Document d = docOwnedBy(1L);
        when(documents.findById(5L)).thenReturn(Optional.of(d));
        CustomUserDetails hod = principal(7L, "HOD");

        assertThrows(AccessDeniedException.class, () -> service.delete(5L, hod));
        verify(documents, never()).delete(any(Document.class));
    }

    @Test
    void adminCanDelete() {
        Document d = docOwnedBy(1L);
        when(documents.findById(5L)).thenReturn(Optional.of(d));

        service.delete(5L, principal(99L, "ADMIN"));

        verify(documents).delete(d);
    }
}