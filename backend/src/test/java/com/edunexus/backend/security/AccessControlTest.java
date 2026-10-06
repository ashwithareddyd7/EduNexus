package com.edunexus.backend.security;

import com.edunexus.backend.AbstractIntegrationTest;
import com.edunexus.backend.TestDataFactory;
import com.edunexus.backend.TestDataFactory.StaffData;
import com.edunexus.backend.TestDataFactory.StudentData;
import com.edunexus.backend.entity.*;
import com.edunexus.backend.repository.DocumentRepository;
import com.edunexus.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * "Change the ID in the URL" and related access-control attacks.
 * Each denied case is paired with a positive control, so a passing test means
 * "blocked on purpose", not "everything is broken".
 */
class AccessControlTest extends AbstractIntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired TestDataFactory data;
    @Autowired UserRepository users;
    @Autowired DocumentRepository documents;

    Department deptA, deptB;
    Course courseA, courseB;
    StudentData studentA, studentB, studentInOtherDept, extraInOtherDept;
    StaffData hodA, admin;
    Document docB;
    Subject subjectB;

    @BeforeEach
    void setUp() {
        deptA = data.newDepartment();
        deptB = data.newDepartment();
        courseA = data.newCourse(deptA);
        courseB = data.newCourse(deptB);

        studentA = data.newStudent(courseA);          // same department as the HOD
        studentB = data.newStudent(courseA);          // same department, different student
        studentInOtherDept = data.newStudent(courseB);
        extraInOtherDept = data.newStudent(courseB);  // makes dept B bigger than dept A

        hodA = data.newHod(deptA);
        admin = data.newAdmin();

        docB = data.newDocument(studentB.profile());
        subjectB = data.newSubject(courseA);
        data.newRecord(studentB.profile(), subjectB);
        data.newDocument(studentInOtherDept.profile());
    }

    private static ResultMatcher forbiddenOrNotFound() {
        return status().is(anyOf(is(403), is(404)));
    }

    private static String bearer(String token) {
        return "Bearer " + token;
    }

    // ===================== Student A attacks Student B =====================

    @Test
    void studentCannotReadAnotherStudentsProfile() throws Exception {
        mvc.perform(get("/api/students/{id}", studentB.profile().getId())
                .header("Authorization", bearer(studentA.token())))
           .andExpect(forbiddenOrNotFound());
    }

    @Test
    void studentCanReadOwnProfile_positiveControl() throws Exception {
        mvc.perform(get("/api/students/{id}", studentA.profile().getId())
                .header("Authorization", bearer(studentA.token())))
           .andExpect(status().isOk());
        mvc.perform(get("/api/students/me")
                .header("Authorization", bearer(studentA.token())))
           .andExpect(status().isOk());
    }

    @Test
    void studentCannotReadAnotherStudentsRecordsOrGpa() throws Exception {
        mvc.perform(get("/api/students/{id}/records", studentB.profile().getId())
                .header("Authorization", bearer(studentA.token())))
           .andExpect(forbiddenOrNotFound());
        mvc.perform(get("/api/students/{id}/gpa", studentB.profile().getId())
                .header("Authorization", bearer(studentA.token())))
           .andExpect(forbiddenOrNotFound());
    }

    @Test
    void studentCanReadOwnRecords_positiveControl() throws Exception {
        mvc.perform(get("/api/students/{id}/records", studentA.profile().getId())
                .header("Authorization", bearer(studentA.token())))
           .andExpect(status().isOk());
    }

    @Test
    void studentCannotListAnotherStudentsDocuments() throws Exception {
        mvc.perform(get("/api/students/{id}/documents", studentB.profile().getId())
                .header("Authorization", bearer(studentA.token())))
           .andExpect(forbiddenOrNotFound());
        mvc.perform(get("/api/students/me/documents")
                .header("Authorization", bearer(studentA.token())))
           .andExpect(status().isOk());
    }

    @Test
    void studentCannotDownloadAnotherStudentsDocument() throws Exception {
        mvc.perform(get("/api/documents/{id}/download", docB.getId())
                .header("Authorization", bearer(studentA.token())))
           .andExpect(forbiddenOrNotFound());
    }

    @Test
    void studentCannotDeleteAnotherStudentsDocument() throws Exception {
        mvc.perform(delete("/api/documents/{id}", docB.getId())
                .header("Authorization", bearer(studentA.token())))
           .andExpect(forbiddenOrNotFound());
        assertThat(documents.existsById(docB.getId()))
                .as("document must still exist after the attack").isTrue();
    }

    // ===================== Student tries staff features =====================

    @Test
    void studentCannotUseHodOrAdminEndpoints() throws Exception {
        String t = bearer(studentA.token());
        mvc.perform(get("/api/hod/stats/overview").header("Authorization", t))
           .andExpect(status().isForbidden());
        mvc.perform(get("/api/hod/documents").header("Authorization", t))
           .andExpect(status().isForbidden());
        mvc.perform(get("/api/admin/stats/departments").header("Authorization", t))
           .andExpect(status().isForbidden());
        mvc.perform(get("/api/students").header("Authorization", t))
           .andExpect(status().isForbidden());
        mvc.perform(put("/api/records").header("Authorization", t)
                .contentType(APPLICATION_JSON).content("{}"))
           .andExpect(status().isForbidden());
        mvc.perform(post("/api/subjects").header("Authorization", t)
                .contentType(APPLICATION_JSON).content("{}"))
           .andExpect(status().isForbidden());
        mvc.perform(patch("/api/admin/students/{id}/status", studentB.profile().getId())
                .header("Authorization", t)
                .contentType(APPLICATION_JSON).content("{\"enabled\":false}"))
           .andExpect(status().isForbidden());
    }

    @Test
    void hodCannotUseAdminOnlyEndpoints() throws Exception {
        String t = bearer(hodA.token());
        mvc.perform(get("/api/admin/stats/departments").header("Authorization", t))
           .andExpect(status().isForbidden());
        mvc.perform(patch("/api/admin/students/{id}/status", studentB.profile().getId())
                .header("Authorization", t)
                .contentType(APPLICATION_JSON).content("{\"enabled\":false}"))
           .andExpect(status().isForbidden());
    }

    @Test
    void hodCannotDeleteDocuments() throws Exception {
        mvc.perform(delete("/api/documents/{id}", docB.getId())
                .header("Authorization", bearer(hodA.token())))
           .andExpect(status().isForbidden());
    }

    // ===================== HOD across departments =====================

    @Test
    void hodCanSeeStudentInOwnDepartment_positiveControl() throws Exception {
        mvc.perform(get("/api/students/{id}", studentA.profile().getId())
                .header("Authorization", bearer(hodA.token())))
           .andExpect(status().isOk());
        mvc.perform(get("/api/students/{id}/records", studentB.profile().getId())
                .header("Authorization", bearer(hodA.token())))
           .andExpect(status().isOk());
    }

    @Test
    void hodCannotSeeStudentFromAnotherDepartment() throws Exception {
        long otherId = studentInOtherDept.profile().getId();
        String t = bearer(hodA.token());
        mvc.perform(get("/api/students/{id}", otherId).header("Authorization", t))
           .andExpect(forbiddenOrNotFound());
        mvc.perform(get("/api/students/{id}/records", otherId).header("Authorization", t))
           .andExpect(forbiddenOrNotFound());
        mvc.perform(get("/api/students/{id}/gpa", otherId).header("Authorization", t))
           .andExpect(forbiddenOrNotFound());
        mvc.perform(get("/api/students/{id}/documents", otherId).header("Authorization", t))
           .andExpect(forbiddenOrNotFound());
    }

    @Test
    void hodCannotDownloadDocumentFromAnotherDepartment() throws Exception {
        Document otherDoc = data.newDocument(extraInOtherDept.profile());
        mvc.perform(get("/api/documents/{id}/download", otherDoc.getId())
                .header("Authorization", bearer(hodA.token())))
           .andExpect(forbiddenOrNotFound());
    }

    @Test
    void hodPassingAnotherDepartmentIdGetsNoExtraData() throws Exception {
        String t = bearer(hodA.token());
        String own = mvc.perform(get("/api/hod/stats/overview").header("Authorization", t))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        MockHttpServletResponse res = mvc.perform(get("/api/hod/stats/overview")
                        .param("departmentId", String.valueOf(deptB.getId()))
                        .header("Authorization", t))
                .andReturn().getResponse();

        if (res.getStatus() == 200) {
            assertThat(res.getContentAsString())
                    .as("a HOD must always get their OWN department's numbers").isEqualTo(own);
        } else {
            assertThat(res.getStatus()).isEqualTo(403);
        }
    }

    @Test
    void hodCannotListDocumentsOfAnotherDepartment() throws Exception {
        String t = bearer(hodA.token());
        String own = mvc.perform(get("/api/hod/documents").header("Authorization", t))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        MockHttpServletResponse res = mvc.perform(get("/api/hod/documents")
                        .param("departmentId", String.valueOf(deptB.getId()))
                        .header("Authorization", t))
                .andReturn().getResponse();
        if (res.getStatus() == 200) {
            assertThat(res.getContentAsString()).isEqualTo(own);
        } else {
            assertThat(res.getStatus()).isEqualTo(403);
        }
    }

    @Test
    void adminCanSeeAnyStudent_positiveControl() throws Exception {
        mvc.perform(get("/api/students/{id}", studentInOtherDept.profile().getId())
                .header("Authorization", bearer(admin.token())))
           .andExpect(status().isOk());
        mvc.perform(get("/api/admin/stats/departments")
                .header("Authorization", bearer(admin.token())))
           .andExpect(status().isOk());
    }

    // ===================== Tokens =====================

    @Test
    void noTokenIsRejected() throws Exception {
        mvc.perform(get("/api/students/me")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/students/{id}", studentA.profile().getId()))
           .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/documents/{id}/download", docB.getId()))
           .andExpect(status().isUnauthorized());
    }

    @Test
    void garbageTokenIsRejected() throws Exception {
        mvc.perform(get("/api/students/me").header("Authorization", "Bearer not.a.token"))
           .andExpect(status().isUnauthorized());
    }

    @Test
    void expiredTokenIsRejected() throws Exception {
        mvc.perform(get("/api/students/me")
                .header("Authorization", bearer(data.expiredTokenFor(studentA.user().getEmail()))))
           .andExpect(status().isUnauthorized());
    }

    @Test
    void tokenSignedWithWrongKeyIsRejected() throws Exception {
        mvc.perform(get("/api/students/me")
                .header("Authorization", bearer(data.tokenSignedWithWrongKey(studentA.user().getEmail()))))
           .andExpect(status().isUnauthorized());
    }

    @Test
    void tamperedTokenIsRejected() throws Exception {
        String token = studentA.token();
        String tampered = token.substring(0, token.length() - 3)
                + (token.endsWith("abc") ? "xyz" : "abc");
        mvc.perform(get("/api/students/me").header("Authorization", bearer(tampered)))
           .andExpect(status().isUnauthorized());
    }

    @Test
    void validTokenWorks_positiveControl() throws Exception {
        mvc.perform(get("/api/auth/me").header("Authorization", bearer(studentA.token())))
           .andExpect(status().isOk());
    }

    @Test
    void disabledUsersTokenStopsWorkingImmediately() throws Exception {
        mvc.perform(get("/api/auth/me").header("Authorization", bearer(studentA.token())))
           .andExpect(status().isOk());

        User u = studentA.user();
        u.setEnabled(false);
        users.save(u);

        mvc.perform(get("/api/auth/me").header("Authorization", bearer(studentA.token())))
           .andExpect(status().isUnauthorized());
    }

    // ===================== Mass assignment / privilege escalation =====================

    @Test
    void studentCannotMakeThemselvesAdminViaProfileUpdate() throws Exception {
        mvc.perform(put("/api/students/me")
                .header("Authorization", bearer(studentA.token()))
                .contentType(APPLICATION_JSON)
                .content("{\"fullName\":\"Hacker\",\"role\":\"ADMIN\",\"id\":1,\"userId\":1,\"enabled\":true}"));
        // Whatever the status was, the real proof is that admin doors stay shut:
        mvc.perform(get("/api/admin/stats/departments")
                .header("Authorization", bearer(studentA.token())))
           .andExpect(status().isForbidden());
    }

    @Test
    void registrationCannotCreateAnAdmin() throws Exception {
        String email = "evil." + System.nanoTime() + "@test.com";
        MockHttpServletResponse res = mvc.perform(post("/api/auth/register")
                        .contentType(APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"" + TestDataFactory.PASSWORD
                                + "\",\"fullName\":\"Evil\",\"role\":\"ADMIN\"}"))
                .andReturn().getResponse();
        assumeTrue(res.getStatus() == 201,
                "Register returned " + res.getStatus() + ": adjust the JSON to match RegisterRequest");

        mvc.perform(get("/api/admin/stats/departments")
                .header("Authorization", bearer(data.tokenFor(email))))
           .andExpect(status().isForbidden());
    }

    // ===================== Data leaks =====================

    @Test
    void responsesNeverContainPasswordHashes() throws Exception {
        String t = bearer(studentA.token());
        for (String url : new String[]{"/api/auth/me", "/api/students/me", "/api/students/me/documents"}) {
            String body = mvc.perform(get(url).header("Authorization", t))
                    .andReturn().getResponse().getContentAsString();
            assertThat(body).as(url).doesNotContainIgnoringCase("passwordHash")
                    .doesNotContain("$2a$").doesNotContain("$2b$");
        }
        String hodList = mvc.perform(get("/api/students").header("Authorization", bearer(hodA.token())))
                .andReturn().getResponse().getContentAsString();
        assertThat(hodList).doesNotContainIgnoringCase("passwordHash").doesNotContain("$2a$");
    }

    @Test
    void loginDoesNotRevealWhetherTheEmailExists() throws Exception {
        String wrongPassword = "{\"email\":\"" + studentA.user().getEmail() + "\",\"password\":\"wrong-password-1\"}";
        String unknownEmail = "{\"email\":\"nobody." + System.nanoTime() + "@test.com\",\"password\":\"wrong-password-1\"}";

        MockHttpServletResponse a = mvc.perform(post("/api/auth/login")
                .contentType(APPLICATION_JSON).content(wrongPassword)).andReturn().getResponse();
        MockHttpServletResponse b = mvc.perform(post("/api/auth/login")
                .contentType(APPLICATION_JSON).content(unknownEmail)).andReturn().getResponse();

        assertThat(a.getStatus()).isEqualTo(401);
        assertThat(b.getStatus()).isEqualTo(a.getStatus());
    }
}