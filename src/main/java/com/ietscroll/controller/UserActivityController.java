package com.ietscroll.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ietscroll.entity.UserActivityLog;
import com.ietscroll.response.Result;
import com.ietscroll.service.UserActivityLogService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/user/activities")
@Tag(name = "User Activity Audit Log", description = "Server-side cross-device audit history of user submissions and moderation verdicts")
public class UserActivityController {

    private final UserActivityLogService activityLogService;

    public UserActivityController(UserActivityLogService activityLogService) {
        this.activityLogService = activityLogService;
    }

    @GetMapping
    @Operation(summary = "Get user audit history", description = "Fetches cross-device server-side activity history for authenticated student.")
    public List<UserActivityLog> getActivities(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return List.of();
        }
        return activityLogService.getUserActivities(authentication.getName());
    }

    @DeleteMapping
    @Operation(summary = "Clear user audit log", description = "Clears activity log history for authenticated student.")
    public Result clearActivities(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return Result.FAILED;
        }
        return activityLogService.clearUserActivities(authentication.getName());
    }
}
