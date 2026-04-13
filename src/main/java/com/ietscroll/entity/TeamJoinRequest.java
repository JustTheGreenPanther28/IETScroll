package com.ietscroll.entity;

import java.time.LocalDateTime;

import com.ietscroll.general.enums.TeamRequestStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "team_join_requests")
public class TeamJoinRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "applicant_email", referencedColumnName = "email", nullable = false)
    private UserEntity applicant;

    @ManyToOne
    @JoinColumn(name = "team_id", referencedColumnName = "publicId", nullable = false)
    private Team requestedTeam;

    @Column(nullable = false, length = 300)
    private String message;

    private LocalDateTime requestedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TeamRequestStatus status = TeamRequestStatus.WAIT;

    @PrePersist
    public void onCreate() {
        this.requestedAt = LocalDateTime.now();
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

	public TeamRequestStatus getStatus() {
		return status;
	}

	public void setStatus(TeamRequestStatus status) {
		this.status = status;
	}

	public int getId() {
		return id;
	}

	public LocalDateTime getRequestedAt() {
		return requestedAt;
	}
    
}