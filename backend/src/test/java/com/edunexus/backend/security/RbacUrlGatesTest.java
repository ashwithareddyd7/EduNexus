package com.edunexus.backend.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.edunexus.backend.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@WebMvcTest(controllers = RbacUrlGatesTest.ProbeController.class)
@Import({SecurityConfig.class, RbacUrlGatesTest.ProbeController.class})
class RbacUrlGatesTest {

    @Autowired MockMvc mvc;
    @MockitoBean JwtService jwtService;
    @MockitoBean CustomUserDetailsService userDetailsService;

    @Test
    void noToken_returns401WithStandardJson() throws Exception {
        mvc.perform(get("/api/any/ping"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test @WithMockUser(roles = "STUDENT")
    void student_cannotReachAdmin() throws Exception {
        mvc.perform(get("/api/admin/ping")).andExpect(status().isForbidden());
    }

    @Test @WithMockUser(roles = "STUDENT")
    void student_cannotReachHod() throws Exception {
        mvc.perform(get("/api/hod/ping")).andExpect(status().isForbidden());
    }

    @Test @WithMockUser(roles = "HOD")
    void hod_canReachHod() throws Exception {
        mvc.perform(get("/api/hod/ping")).andExpect(status().isOk());
    }

    @Test @WithMockUser(roles = "HOD")
    void hod_cannotReachAdmin() throws Exception {
        mvc.perform(get("/api/admin/ping")).andExpect(status().isForbidden());
    }

    @Test @WithMockUser(roles = "ADMIN")
    void admin_canReachAdmin() throws Exception {
        mvc.perform(get("/api/admin/ping")).andExpect(status().isOk());
    }

    @Test @WithMockUser(roles = "ADMIN")
    void admin_canReachHod() throws Exception {
        mvc.perform(get("/api/hod/ping")).andExpect(status().isOk());
    }

    @Test @WithMockUser(roles = "HOD")
    void preAuthorize_denial_returns403() throws Exception {
        mvc.perform(get("/api/student/ping")).andExpect(status().isForbidden());
    }

    @RestController
    public static class ProbeController {
        @GetMapping("/api/admin/ping")   public String admin()   { return "ok"; }
        @GetMapping("/api/hod/ping")     public String hod()     { return "ok"; }
        @GetMapping("/api/any/ping")     public String any()     { return "ok"; }
        @GetMapping("/api/student/ping") @PreAuthorize("hasRole('STUDENT')")
        public String student() { return "ok"; }
    }
}