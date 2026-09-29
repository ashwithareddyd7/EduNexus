package com.edunexus.backend.repository;

import com.edunexus.backend.entity.StaffProfile;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StaffProfileRepository extends JpaRepository<StaffProfile, Long> {

    Optional<StaffProfile> findByUserId(Long userId);
}