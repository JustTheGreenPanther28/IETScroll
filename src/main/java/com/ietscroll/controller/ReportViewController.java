package com.ietscroll.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ietscroll.entity.ReportView;
import com.ietscroll.response.Result;
import com.ietscroll.service.ReportViewService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/report-views")
@Tag(name = "Report Views Audit", description = "Track and display who viewed lost/found reports")
public class ReportViewController {

    private final ReportViewService reportViewService;

    public ReportViewController(ReportViewService reportViewService) {
        this.reportViewService = reportViewService;
    }

    @PostMapping("/{reportId}")
    @Operation(summary = "Record view for report", description = "Records a view on a lost or found report by the authenticated student.")
    public Result recordView(Authentication auth, @PathVariable UUID reportId, @RequestParam(defaultValue = "LOST") String type) {
        if (auth == null || auth.getName() == null) {
            return Result.FAILED;
        }
        return reportViewService.recordView(reportId, type, auth.getName());
    }

    @GetMapping("/{reportId}")
    @Operation(summary = "Get viewers of a report", description = "Retrieves list of users who viewed this specific report.")
    public List<ReportView> getReportViews(Authentication auth, @PathVariable UUID reportId) {
        return reportViewService.getViewsForReport(reportId, auth != null ? auth.getName() : null);
    }
}
