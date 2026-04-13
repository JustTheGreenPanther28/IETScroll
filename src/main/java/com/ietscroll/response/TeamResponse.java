package com.ietscroll.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.ietscroll.general.enums.TeamStatus;

public class TeamResponse {

	private UUID publicId;
	
	private String createdBy;
	
	private String purpose;
	
	private int maxMember;
	
	private LocalDateTime createdAt;
	
	private TeamStatus status;
	
	public UUID getPublicId() {
		return publicId;
	}
	public void setPublicId(UUID publicId) {
		this.publicId = publicId;
	}
	public String getCreatedBy() {
		return createdBy;
	}
	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}
	public String getPurpose() {
		return purpose;
	}
	public void setPurpose(String purpose) {
		this.purpose = purpose;
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
	public TeamStatus getStatus() {
		return status;
	}
	public void setStatus(TeamStatus status) {
		this.status = status;
	}
	
}
