package com.ietscroll.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(name = "resume_usage_audit", indexes = {
    @Index(name = "idx_resume_usage_user_month", columnList = "userEmail, yearMonth")
})
public class ResumeUsageAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String userEmail;

    @Column(nullable = false, length = 7)
    private String yearMonth; // Format "YYYY-MM", e.g. "2026-11"

    @Column(nullable = false)
    private LocalDateTime analyzedAt = LocalDateTime.now();

    public ResumeUsageAudit() {}

    public ResumeUsageAudit(String userEmail, String yearMonth) {
        this.userEmail = userEmail;
        this.yearMonth = yearMonth;
        this.analyzedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getYearMonth() {
        return yearMonth;
    }

    public void setYearMonth(String yearMonth) {
        this.yearMonth = yearMonth;
    }

    public LocalDateTime getAnalyzedAt() {
        return analyzedAt;
    }

    public void setAnalyzedAt(LocalDateTime analyzedAt) {
        this.analyzedAt = analyzedAt;
    }
}
