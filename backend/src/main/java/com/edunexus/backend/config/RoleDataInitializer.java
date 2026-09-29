package com.edunexus.backend.config;

import com.edunexus.backend.entity.Role;
import com.edunexus.backend.entity.RoleName;
import com.edunexus.backend.repository.RoleRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Makes sure the three roles exist at startup. Safe to run repeatedly (only inserts missing ones). */
@Component
public class RoleDataInitializer implements ApplicationRunner {

    private final RoleRepository roleRepository;

    public RoleDataInitializer(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        for (RoleName roleName : RoleName.values()) {
            if (!roleRepository.existsByName(roleName)) {
                roleRepository.save(new Role(roleName));
            }
        }
    }
}