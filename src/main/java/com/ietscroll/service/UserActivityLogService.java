package com.ietscroll.service;

import java.util.List;

import com.ietscroll.entity.UserActivityLog;
import com.ietscroll.response.Result;

public interface UserActivityLogService {
    void logActivity(String userEmail, String activityType, String title, String status, String message);
    List<UserActivityLog> getUserActivities(String userEmail);
    Result clearUserActivities(String userEmail);
}
