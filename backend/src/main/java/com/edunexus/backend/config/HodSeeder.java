package com.edunexus.backend.config;

import com.edunexus.backend.entity.Department;
import com.edunexus.backend.entity.Role;
import com.edunexus.backend.entity.RoleName;
import com.edunexus.backend.entity.StaffProfile;
import com.edunexus.backend.entity.User;
import com.edunexus.backend.repository.DepartmentRepository;
import com.edunexus.backend.repository.RoleRepository;
import com.edunexus.backend.repository.StaffProfileRepository;
import com.edunexus.backend.repository.UserRepository;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Creates a development HOD for the CSE department if it does not exist yet.
 * Needs the HOD role (RoleDataInitializer) and the CSE department to exist.
 */
@Component
@Order(20)
public class HodSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(HodSeeder.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final DepartmentRepository departmentRepository;
    private final StaffProfileRepository staffProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final String hodEmail;
    private final String hodPassword;

    public HodSeeder(UserRepository userRepository,
                     RoleRepository roleRepository,
                     DepartmentRepository departmentRepository,
                     StaffProfileRepository staffProfileRepository,
                     PasswordEncoder passwordEncoder,
                     @Value("${app.seed.hod-email}") String hodEmail,
                     @Value("${app.seed.hod-password}") String hodPassword) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.departmentRepository = departmentRepository;
        this.staffProfileRepository = staffProfileRepository;
        this.passwordEncoder = passwordEncoder;
        this.hodEmail = hodEmail.trim().toLowerCase(Locale.ROOT);
        this.hodPassword = hodPassword;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (userRepository.existsByEmail(hodEmail)) {
            return;
        }
        if (hodPassword == null || hodPassword.length() < 8) {
            throw new IllegalStateException("HOD_PASSWORD must be set and at least 8 characters long");
        }

        Role hodRole = roleRepository.findByName(RoleName.HOD)
                .orElseThrow(() -> new IllegalStateException("HOD role is missing; check role seeding"));

        // ASSUMPTION 1: DepartmentRepository has findByCode(String)
        Department cse = departmentRepository.findByCode("CSE").orElse(null);
        if (cse == null) {
            log.warn("CSE department not found; HOD not seeded. Seed departments first.");
            return;
        }

        User user = userRepository.save(
                new User(hodEmail, passwordEncoder.encode(hodPassword), hodRole));

        // ASSUMPTION 2: StaffProfile has a constructor (User, String fullName, Department)
        staffProfileRepository.save(new StaffProfile(user, "Dev HOD", cse));

        log.info("Seeded HOD account: {}", hodEmail);
    }
}