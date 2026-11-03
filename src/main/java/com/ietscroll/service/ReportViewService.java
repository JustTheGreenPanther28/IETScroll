package com.ietscroll.service;

import java.util.List;
import java.util.UUID;

import com.ietscroll.entity.ReportView;
import com.ietscroll.response.Result;

public interface ReportViewService {
    Result recordView(UUID reportId, String reportType, String viewerEmail);
    List<ReportView> getViewsForReport(UUID reportId, String requesterEmail);
}
