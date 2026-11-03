package com.ietscroll.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ietscroll.dto.AdminAnalyticsDTO;
import com.ietscroll.dto.UserAuditDTO;
import com.ietscroll.general.enums.PrivilegeClass;
import com.ietscroll.response.Result;
import com.ietscroll.service.AdminService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/admin")
@Tag(name = "Admin Governance", description = "Privileged administrator control center endpoints")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/users")
    @Operation(summary = "Search and audit users", description = "Admin search users by query, privilege, blocked status.")
    public Page<UserAuditDTO> searchUsers(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) PrivilegeClass privilege,
            @RequestParam(required = false) Boolean isBlocked,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return adminService.searchUsers(query, privilege, isBlocked, pageable);
    }

    @PostMapping("/users/{email}/block")
    @Operation(summary = "Block user", description = "Blocks user login and suspends all their active posts.")
    public Result blockUser(@PathVariable String email) {
        return adminService.blockUser(email);
    }

    @PostMapping("/users/{email}/unblock")
    @Operation(summary = "Unblock user", description = "Unblocks user and restores their suspended posts.")
    public Result unblockUser(@PathVariable String email) {
        return adminService.unblockUser(email);
    }

    @PatchMapping("/users/{email}/role")
    @Operation(summary = "Assign or revoke user role", description = "Assigns role (STUDENT, ADMIN, SENIOR_ADMIN). Only SENIOR_ADMIN can assign or revoke ADMIN and SENIOR_ADMIN roles.")
    public Result updateRole(@PathVariable String email, @RequestParam com.ietscroll.security.Role role, org.springframework.security.core.Authentication authentication) {
        String callerEmail = authentication != null ? authentication.getName() : null;
        return adminService.updateUserRole(email, role, callerEmail);
    }

    @PatchMapping("/users/{email}/privilege")
    @Operation(summary = "Update user privilege tier", description = "Assigns privilege class (TRUSTED, MAY_BE_SPAM, NEW, NORMAL).")
    public Result updatePrivilege(@PathVariable String email, @RequestParam PrivilegeClass privilege) {
        return adminService.updateUserPrivilege(email, privilege);
    }

    @PatchMapping("/lost-item/{publicId}/close")
    @Operation(summary = "Admin close lost item", description = "Allows admin to close any lost item report.")
    public Result closeLostItem(@PathVariable UUID publicId) {
        return adminService.closeLostItem(publicId);
    }

    @PatchMapping("/found-item/{publicId}/close")
    @Operation(summary = "Admin close found item", description = "Allows admin to close any found item report.")
    public Result closeFoundItem(@PathVariable UUID publicId) {
        return adminService.closeFoundItem(publicId);
    }

    @PatchMapping("/team/{publicId}/close")
    @Operation(summary = "Admin close team", description = "Allows admin to close any student team.")
    public Result closeTeam(@PathVariable UUID publicId) {
        return adminService.closeTeam(publicId);
    }

    @GetMapping("/analytics")
    @Operation(summary = "Get platform analytics", description = "Returns summary counts and time-series growth for reports and users.")
    public AdminAnalyticsDTO getAnalytics() {
        return adminService.getAnalytics();
    }
}
