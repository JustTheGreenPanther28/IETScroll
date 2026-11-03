package com.ietscroll.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(name = "report_view", indexes = {
    @Index(name = "idx_report_view_report_viewer", columnList = "reportId, viewerEmail")
})
public class ReportView {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private UUID reportId; // public UUID of Lost or Found item

    @Column(nullable = false, length = 20)
    private String reportType; // "LOST" or "FOUND"

    @Column(nullable = false, length = 100)
    private String reportOwnerEmail;

    @Column(nullable = false, length = 100)
    private String viewerEmail;

    @Column(length = 100)
    private String viewerName;

    @Column(nullable = false)
    private LocalDateTime viewedAt = LocalDateTime.now();

    public ReportView() {}

    public ReportView(UUID reportId, String reportType, String reportOwnerEmail, String viewerEmail, String viewerName) {
        this.reportId = reportId;
        this.reportType = reportType;
        this.reportOwnerEmail = reportOwnerEmail;
        this.viewerEmail = viewerEmail;
        this.viewerName = viewerName;
        this.viewedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public UUID getReportId() {
        return reportId;
    }

    public void setReportId(UUID reportId) {
        this.reportId = reportId;
    }

    public String getReportType() {
        return reportType;
    }

    public void setReportType(String reportType) {
        this.reportType = reportType;
    }

    public String getReportOwnerEmail() {
        return reportOwnerEmail;
    }

    public void setReportOwnerEmail(String reportOwnerEmail) {
        this.reportOwnerEmail = reportOwnerEmail;
    }

    public String getViewerEmail() {
        return viewerEmail;
    }

    public void setViewerEmail(String viewerEmail) {
        this.viewerEmail = viewerEmail;
    }

    public String getViewerName() {
        return viewerName;
    }

    public void setViewerName(String viewerName) {
        this.viewerName = viewerName;
    }

    public LocalDateTime getViewedAt() {
        return viewedAt;
    }

    public void setViewedAt(LocalDateTime viewedAt) {
        this.viewedAt = viewedAt;
    }
}
