package com.ietscroll.service.impl;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ietscroll.dto.AdminAnalyticsDTO;
import com.ietscroll.dto.UserAuditDTO;
import com.ietscroll.entity.UserEntity;
import com.ietscroll.exception.ResourceNotFoundException;
import com.ietscroll.general.enums.FoundItemStatus;
import com.ietscroll.general.enums.LostItemStatus;
import com.ietscroll.general.enums.PrivilegeClass;
import com.ietscroll.general.enums.TeamStatus;
import com.ietscroll.repository.FoundItemRepository;
import com.ietscroll.repository.LostItemRepository;
import com.ietscroll.repository.TeamRepository;
import com.ietscroll.repository.UserRepository;
import com.ietscroll.response.Result;
import com.ietscroll.service.AdminService;

@Service
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final LostItemRepository lostItemRepository;
    private final FoundItemRepository foundItemRepository;
    private final TeamRepository teamRepository;

    public AdminServiceImpl(UserRepository userRepository,
                            LostItemRepository lostItemRepository,
                            FoundItemRepository foundItemRepository,
                            TeamRepository teamRepository) {
        this.userRepository = userRepository;
        this.lostItemRepository = lostItemRepository;
        this.foundItemRepository = foundItemRepository;
        this.teamRepository = teamRepository;
    }

    @Override
    public Page<UserAuditDTO> searchUsers(String query, PrivilegeClass privilege, Boolean isBlocked, Pageable pageable) {
        Page<UserEntity> page = userRepository.searchUsers(query, privilege, isBlocked, pageable);
        return page.map(user -> {
            UserAuditDTO dto = new UserAuditDTO();
            dto.setUserId(user.getUserId());
            dto.setPublicUserId(user.getPublicUserId());
            dto.setUsername(user.getUsername());
            dto.setFullName(user.getFullName());
            dto.setEmail(user.getEmail());
            dto.setYearOfPassout(user.getYearOfPassout());
            dto.setCourse(user.getCourse());
            dto.setBranch(user.getBranch());
            dto.setRole(user.getRole());
            dto.setPrivilegeClass(user.getPrivilegeClass());
            dto.setBlocked(user.isBlocked());
            dto.setLastLoginAt(user.getLastLoginAt());
            dto.setLastLogoutAt(user.getLastLogoutAt());
            dto.setCreatedAt(user.getCreatedAt());

            dto.setLostItemsCount(lostItemRepository.countTotalByUser(user.getEmail()));
            dto.setFoundItemsCount(foundItemRepository.countTotalByUser(user.getEmail()));
            dto.setTeamsCreatedCount(teamRepository.countByCreatedBy_Email(user.getEmail()));
            return dto;
        });
    }

    @Override
    @Transactional
    public Result blockUser(String email) {
        UserEntity user = userRepository.findByEmail(email);
        if (user == null) {
            throw new ResourceNotFoundException("User not found with email: " + email);
        }
        user.setBlocked(true);
        userRepository.save(user);

        // Suspend user's active lost items, found items, and teams
        lostItemRepository.suspendLostItemsByUser(email);
        foundItemRepository.suspendFoundItemsByUser(email);
        teamRepository.suspendTeamsByUser(email);

        return Result.SUCCESS;
    }

    @Override
    @Transactional
    public Result unblockUser(String email) {
        UserEntity user = userRepository.findByEmail(email);
        if (user == null) {
            throw new ResourceNotFoundException("User not found with email: " + email);
        }
        user.setBlocked(false);
        userRepository.save(user);

        // Restore suspended items/teams
        lostItemRepository.restoreLostItemsByUser(email);
        foundItemRepository.restoreFoundItemsByUser(email);
        teamRepository.restoreTeamsByUser(email);

        return Result.SUCCESS;
    }

    @Override
    @Transactional
    public Result updateUserPrivilege(String email, PrivilegeClass privilege) {
        UserEntity user = userRepository.findByEmail(email);
        if (user == null) {
            throw new ResourceNotFoundException("User not found with email: " + email);
        }
        user.setPrivilegeClass(privilege);
        userRepository.save(user);
        return Result.SUCCESS;
    }

    @Override
    @Transactional
    public Result closeLostItem(UUID publicId) {
        int updated = lostItemRepository.closeRequestByAdmin(publicId);
        return updated > 0 ? Result.SUCCESS : Result.FAILED;
    }

    @Override
    @Transactional
    public Result closeFoundItem(UUID publicId) {
        int updated = foundItemRepository.closeRequestByAdmin(publicId);
        return updated > 0 ? Result.SUCCESS : Result.FAILED;
    }

    @Override
    @Transactional
    public Result closeTeam(UUID publicId) {
        int updated = teamRepository.closeTeamByAdmin(publicId);
        return updated > 0 ? Result.SUCCESS : Result.FAILED;
    }

    @Override
    public AdminAnalyticsDTO getAnalytics() {
        AdminAnalyticsDTO dto = new AdminAnalyticsDTO();
        long totalUsers = userRepository.count();
        long blockedUsers = userRepository.countByIsBlocked(true);
        dto.setTotalUsers(totalUsers);
        dto.setBlockedUsers(blockedUsers);
        dto.setActiveUsers(totalUsers - blockedUsers);

        dto.setTotalLostReports(lostItemRepository.count());
        dto.setTotalFoundReports(foundItemRepository.count());
        dto.setTotalTeamsCreated(teamRepository.count());

        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);

        dto.setUserGrowth(toMap(userRepository.getUserGrowthTimeline(thirtyDaysAgo)));
        dto.setLostReportsGrowth(toMap(lostItemRepository.getLostItemTimeline(thirtyDaysAgo)));
        dto.setFoundReportsGrowth(toMap(foundItemRepository.getFoundItemTimeline(thirtyDaysAgo)));
        dto.setTeamCreationGrowth(toMap(teamRepository.getTeamTimeline(thirtyDaysAgo)));

        return dto;
    }

    private Map<String, Long> toMap(List<Object[]> rows) {
        Map<String, Long> map = new LinkedHashMap<>();
        if (rows == null) return map;
        for (Object[] row : rows) {
            if (row != null && row.length >= 2 && row[0] != null && row[1] != null) {
                map.put(row[0].toString(), ((Number) row[1]).longValue());
            }
        }
        return map;
    }
}
