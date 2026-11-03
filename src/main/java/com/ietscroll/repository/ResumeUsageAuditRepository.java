package com.ietscroll.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ietscroll.entity.ResumeUsageAudit;

@Repository
public interface ResumeUsageAuditRepository extends JpaRepository<ResumeUsageAudit, Long> {
    long countByUserEmailAndYearMonth(String userEmail, String yearMonth);
}
