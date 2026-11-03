package com.ietscroll.service.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ietscroll.entity.ReportView;
import com.ietscroll.entity.UserEntity;
import com.ietscroll.repository.ReportViewRepository;
import com.ietscroll.repository.UserRepository;
import com.ietscroll.repository.LostItemRepository;
import com.ietscroll.repository.FoundItemRepository;
import com.ietscroll.response.Result;
import com.ietscroll.service.ReportViewService;

@Service
public class ReportViewServiceImpl implements ReportViewService {

    private final ReportViewRepository reportViewRepository;
    private final UserRepository userRepository;
    private final LostItemRepository lostItemRepository;
    private final FoundItemRepository foundItemRepository;

    public ReportViewServiceImpl(ReportViewRepository reportViewRepository,
                                 UserRepository userRepository,
                                 LostItemRepository lostItemRepository,
                                 FoundItemRepository foundItemRepository) {
        this.reportViewRepository = reportViewRepository;
        this.userRepository = userRepository;
        this.lostItemRepository = lostItemRepository;
        this.foundItemRepository = foundItemRepository;
    }

    @Override
    @Transactional
    public Result recordView(UUID reportId, String reportType, String viewerEmail) {
        if (reportId == null || viewerEmail == null) {
            return Result.FAILED;
        }

        // Avoid duplicate view recording for same viewer
        if (reportViewRepository.existsByReportIdAndViewerEmail(reportId, viewerEmail)) {
            return Result.SUCCESS;
        }

        String ownerEmail = "";
        if ("LOST".equalsIgnoreCase(reportType)) {
            var item = lostItemRepository.findAll().stream()
                .filter(i -> reportId.equals(i.getPublicIdOfLostRequest()))
                .findFirst().orElse(null);
            if (item != null) ownerEmail = item.getOwnerEmail();
        } else if ("FOUND".equalsIgnoreCase(reportType)) {
            var item = foundItemRepository.findAll().stream()
                .filter(i -> reportId.equals(i.getPublicIdOfFoundItem()))
                .findFirst().orElse(null);
            if (item != null) ownerEmail = item.getContactTo();
        }

        UserEntity viewer = userRepository.findByEmail(viewerEmail);
        String viewerName = viewer != null ? (viewer.getFullName() != null ? viewer.getFullName() : viewer.getUsername()) : viewerEmail;

        ReportView view = new ReportView(reportId, reportType != null ? reportType.toUpperCase() : "ITEM", ownerEmail, viewerEmail, viewerName);
        reportViewRepository.save(view);
        return Result.SUCCESS;
    }

    @Override
    public List<ReportView> getViewsForReport(UUID reportId, String requesterEmail) {
        if (reportId == null) {
            return List.of();
        }
        return reportViewRepository.findByReportIdOrderByViewedAtDesc(reportId);
    }
}
