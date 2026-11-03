package com.ietscroll.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.ietscroll.dto.AdminAnalyticsDTO;
import com.ietscroll.dto.UserAuditDTO;
import com.ietscroll.general.enums.PrivilegeClass;
import com.ietscroll.response.Result;

public interface AdminService {
    Page<UserAuditDTO> searchUsers(String query, PrivilegeClass privilege, Boolean isBlocked, Pageable pageable);
    Result blockUser(String email);
    Result unblockUser(String email);
    Result updateUserPrivilege(String email, PrivilegeClass privilege);
    Result closeLostItem(UUID publicId);
    Result closeFoundItem(UUID publicId);
    Result closeTeam(UUID publicId);
    AdminAnalyticsDTO getAnalytics();
}
