package com.ietscroll.response;

import java.time.LocalDateTime;
import java.time.Year;

import com.ietscroll.general.enums.Branch;
import com.ietscroll.general.enums.Course;
import com.ietscroll.general.enums.TeamRequestStatus;

public class TeamJoinResponse {

	private String applicantEmail;
	
	private String applicantUsername;
	
	private String applicantFullName;
	
	private Course applicantCouse;
	
	private Branch applicantCourse;
	
	private Year yearOfPassout;
	
	private String teamId;
		
	private TeamRequestStatus status;
	
	private String message;
	
    private LocalDateTime requestedAt;
	
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
	
	
	public String getApplicantEmail() {
		return applicantEmail;
	}

	public void setApplicantEmail(String applicantEmail) {
		this.applicantEmail = applicantEmail;
	}

	public String getApplicantUsername() {
		return applicantUsername;
	}

	public void setApplicantUsername(String applicantUsername) {
		this.applicantUsername = applicantUsername;
	}

	public String getApplicantFullName() {
		return applicantFullName;
	}

	public void setApplicantFullName(String applicantFullName) {
		this.applicantFullName = applicantFullName;
	}

	public Course getApplicantCouse() {
		return applicantCouse;
	}

	public void setApplicantCouse(Course applicantCouse) {
		this.applicantCouse = applicantCouse;
	}

	public Branch getApplicantCourse() {
		return applicantCourse;
	}

	public void setApplicantCourse(Branch applicantCourse) {
		this.applicantCourse = applicantCourse;
	}

	public Year getYearOfPassout() {
		return yearOfPassout;
	}

	public void setYearOfPassout(Year yearOfPassout) {
		this.yearOfPassout = yearOfPassout;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
