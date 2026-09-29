package com.edunexus.backend.repository;

import com.edunexus.backend.entity.Remark;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RemarkRepository extends JpaRepository<Remark, Long> {
    List<Remark> findByStudentIdOrderByCreatedAtDesc(Long studentProfileId);
}