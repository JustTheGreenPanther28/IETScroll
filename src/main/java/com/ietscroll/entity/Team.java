package com.ietscroll.entity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.ietscroll.general.enums.Privacy;
import com.ietscroll.general.enums.TeamStatus;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@Entity
@Table(name = "team_finder")
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(unique = true, nullable = false)
    private UUID publicId;

    @ManyToOne
    @JoinColumn(name = "created_by", referencedColumnName = "email", nullable = false)
    private UserEntity createdBy;

    @Column(length = 300, nullable = false)
    private String purpose;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TeamStatus status = TeamStatus.OPEN;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Privacy privacy=Privacy.PUBLIC;

    @Min(1)
    @Max(20)
    @Column(nullable = false)
    private int maxMember;

    private LocalDateTime createdAt;
    

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TeamFinderSkill> neededSkills;

    @OneToMany(mappedBy = "requestedTeam", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TeamJoinRequest> applicants;

    @PrePersist
    public void onCreate() {
        this.publicId = UUID.randomUUID();
        this.createdAt = LocalDateTime.now();
    }

    public long getId() {
        return id;
    }

    public UUID getPublicId() {
        return publicId;
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

	public Privacy getPrivacy() {
		return privacy;
	}

	public void setPrivacy(Privacy privacy) {
		this.privacy = privacy;
	}

	@Override
	public String toString() {
		return "Team [id=" + id + ", publicId=" + publicId + ", createdBy=" + createdBy + ", purpose=" + purpose
				+ ", status=" + status + ", privacy=" + privacy + ", maxMember=" + maxMember + ", createdAt="
				+ createdAt + ", neededSkills=" + neededSkills + ", applicants=" + applicants + "]";
	}
}


