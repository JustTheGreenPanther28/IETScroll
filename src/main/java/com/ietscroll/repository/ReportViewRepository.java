package com.ietscroll.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ietscroll.entity.ReportView;

@Repository
public interface ReportViewRepository extends JpaRepository<ReportView, Long> {

    boolean existsByReportIdAndViewerEmail(UUID reportId, String viewerEmail);

    long countByReportId(UUID reportId);

    List<ReportView> findByReportIdOrderByViewedAtDesc(UUID reportId);

    List<ReportView> findByReportOwnerEmailOrderByViewedAtDesc(String reportOwnerEmail);
}
