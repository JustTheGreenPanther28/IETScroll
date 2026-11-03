package com.ietscroll.dto;

import java.util.Map;

public class AdminAnalyticsDTO {
    private long totalUsers;
    private long activeUsers;
    private long blockedUsers;
    private long totalLostReports;
    private long totalFoundReports;
    private long totalTeamsCreated;

    // Timeline maps: key is Date ("YYYY-MM-DD"), value is Count
    private Map<String, Long> userGrowth;
    private Map<String, Long> lostReportsGrowth;
    private Map<String, Long> foundReportsGrowth;
    private Map<String, Long> teamCreationGrowth;

    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public long getActiveUsers() {
        return activeUsers;
    }

    public void setActiveUsers(long activeUsers) {
        this.activeUsers = activeUsers;
    }

    public long getBlockedUsers() {
        return blockedUsers;
    }

    public void setBlockedUsers(long blockedUsers) {
        this.blockedUsers = blockedUsers;
    }

    public long getTotalLostReports() {
        return totalLostReports;
    }

    public void setTotalLostReports(long totalLostReports) {
        this.totalLostReports = totalLostReports;
    }

    public long getTotalFoundReports() {
        return totalFoundReports;
    }

    public void setTotalFoundReports(long totalFoundReports) {
        this.totalFoundReports = totalFoundReports;
    }

    public long getTotalTeamsCreated() {
        return totalTeamsCreated;
    }

    public void setTotalTeamsCreated(long totalTeamsCreated) {
        this.totalTeamsCreated = totalTeamsCreated;
    }

    public Map<String, Long> getUserGrowth() {
        return userGrowth;
    }

    public void setUserGrowth(Map<String, Long> userGrowth) {
        this.userGrowth = userGrowth;
    }

    public Map<String, Long> getLostReportsGrowth() {
        return lostReportsGrowth;
    }

    public void setLostReportsGrowth(Map<String, Long> lostReportsGrowth) {
        this.lostReportsGrowth = lostReportsGrowth;
    }

    public Map<String, Long> getFoundReportsGrowth() {
        return foundReportsGrowth;
    }

    public void setFoundReportsGrowth(Map<String, Long> foundReportsGrowth) {
        this.foundReportsGrowth = foundReportsGrowth;
    }

    public Map<String, Long> getTeamCreationGrowth() {
        return teamCreationGrowth;
    }

    public void setTeamCreationGrowth(Map<String, Long> teamCreationGrowth) {
        this.teamCreationGrowth = teamCreationGrowth;
    }
}
