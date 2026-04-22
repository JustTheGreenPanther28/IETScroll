package com.ietscroll.response;

import java.time.LocalDateTime;

import com.ietscroll.general.enums.TeamRequestStatus;

public class ApplicationResponse {

	private LocalDateTime requestedAt;

	private String teamId;

	private TeamRequestStatus status;

	private String yourMessage;

	public LocalDateTime getRequestedAt() {
		return requestedAt;
	}

	public void setRequestedAt(LocalDateTime requestedAt) {
		this.requestedAt = requestedAt;
	}

	public String getTeamId() {
		return teamId;
	}

	public void setTeamId(String teamId) {
		this.teamId = teamId;
	}

	public TeamRequestStatus getStatus() {
		return status;
	}

	public void setStatus(TeamRequestStatus status) {
		this.status = status;
	}

	public String getYourMessage() {
		return yourMessage;
	}

	public void setYourMessage(String yourMessage) {
		this.yourMessage = yourMessage;
	}

}
