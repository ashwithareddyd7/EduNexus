package com.edunexus.backend.repository;

import com.edunexus.backend.entity.AuditLog;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByResourceTypeAndResourceIdOrderByTimestampDesc(String resourceType, Long resourceId);
}