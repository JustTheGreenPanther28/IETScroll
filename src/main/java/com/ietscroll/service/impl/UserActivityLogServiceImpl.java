package com.ietscroll.service.impl;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ietscroll.entity.UserActivityLog;
import com.ietscroll.repository.UserActivityLogRepository;
import com.ietscroll.response.Result;
import com.ietscroll.service.UserActivityLogService;

@Service
public class UserActivityLogServiceImpl implements UserActivityLogService {

    private final UserActivityLogRepository activityLogRepository;

    public UserActivityLogServiceImpl(UserActivityLogRepository activityLogRepository) {
        this.activityLogRepository = activityLogRepository;
    }

    @Override
    @Transactional
    public void logActivity(String userEmail, String activityType, String title, String status, String message) {
        if (userEmail == null || userEmail.isBlank()) return;
        UserActivityLog log = new UserActivityLog(userEmail, activityType, title, status, message);
        activityLogRepository.save(log);
    }

    @Override
    public List<UserActivityLog> getUserActivities(String userEmail) {
        if (userEmail == null || userEmail.isBlank()) return List.of();
        // Return latest 50 activities for user
        return activityLogRepository.findByUserEmailOrderByTimestampDesc(userEmail, PageRequest.of(0, 50)).getContent();
    }

    @Override
    @Transactional
    public Result clearUserActivities(String userEmail) {
        if (userEmail == null || userEmail.isBlank()) return Result.FAILED;
        activityLogRepository.deleteByUserEmail(userEmail);
        return Result.SUCCESS;
    }
}
