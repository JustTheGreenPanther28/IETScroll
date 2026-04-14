package com.ietscroll.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.ietscroll.entity.TeamFinderSkill;
import com.ietscroll.entity.TeamJoinRequest;
import com.ietscroll.entity.UserEntity;
import com.ietscroll.general.enums.TeamStatus;

public class TeamDTO {
	private long id;

	private UUID publicId;

	private UserEntity createdBy;

	private String purpose;

	private TeamStatus status = TeamStatus.OPEN;

	private int maxMember;

	private LocalDateTime createdAt;

	private List<TeamFinderSkill> neededSkills;

	private List<TeamJoinRequest> applicants;
	
	private List<Integer> skillIds;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public UUID getPublicId() {
		return publicId;
	}

	public void setPublicId(UUID publicId) {
		this.publicId = publicId;
	}

	public UserEntity getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(UserEntity createdBy) {
		this.createdBy = createdBy;
	}

	public String getPurpose() {
		return purpose;
	}

	public void setPurpose(String purpose) {
		this.purpose = purpose;
	}

	public TeamStatus getStatus() {
		return status;
	}

	public void setStatus(TeamStatus status) {
		this.status = status;
	}

	public int getMaxMember() {
		return maxMember;
	}

	public void setMaxMember(int maxMember) {
		this.maxMember = maxMember;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public List<TeamFinderSkill> getNeededSkills() {
		return neededSkills;
	}

	public void setNeededSkills(List<TeamFinderSkill> neededSkills) {
		this.neededSkills = neededSkills;
	}

	public List<TeamJoinRequest> getApplicants() {
		return applicants;
	}

	public void setApplicants(List<TeamJoinRequest> applicants) {
		this.applicants = applicants;
	}

	public List<Integer> getSkillIds() {
		return skillIds;
	}

	public void setSkillIds(List<Integer> skillIds) {
		this.skillIds = skillIds;
	}

}
