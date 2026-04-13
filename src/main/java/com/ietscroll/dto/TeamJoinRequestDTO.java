package com.ietscroll.dto;

import java.time.LocalDateTime;

import com.ietscroll.entity.Team;
import com.ietscroll.entity.UserEntity;
import com.ietscroll.general.enums.TeamRequestStatus;

public class TeamJoinRequestDTO {
	private int id;

	private UserEntity applicant;

	private Team requestedTeam;

	private String message;

	private LocalDateTime requestedAt;

	private TeamRequestStatus status = TeamRequestStatus.WAIT;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public UserEntity getApplicant() {
		return applicant;
	}

	public void setApplicant(UserEntity applicant) {
		this.applicant = applicant;
	}

	public Team getRequestedTeam() {
		return requestedTeam;
	}

	public void setRequestedTeam(Team requestedTeam) {
		this.requestedTeam = requestedTeam;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public LocalDateTime getRequestedAt() {
		return requestedAt;
	}

	public void setRequestedAt(LocalDateTime requestedAt) {
		this.requestedAt = requestedAt;
	}

	public TeamRequestStatus getStatus() {
		return status;
	}

	public void setStatus(TeamRequestStatus status) {
		this.status = status;
	}
	
	
}
