package com.ietscroll.dto;

import java.time.Year;
import java.util.UUID;

import com.ietscroll.entity.Branch;
import com.ietscroll.entity.Course;

import jakarta.persistence.Column;
import jakarta.validation.constraints.Email;

public class UserDTO {

	private int userId;
	private UUID publicUserId;
	private String userName;
	private String fullName;
	private String email;
	private Year yearOfPassout;
	private Course course;
	private Branch branch;
	private String encryptedPassword;
	public int getUserId() {
		return userId;
	}
	public void setUserId(int userId) {
		this.userId = userId;
	}
	public UUID getPublicUserId() {
		return publicUserId;
	}
	public void setPublicUserId(UUID publicUserId) {
		this.publicUserId = publicUserId;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getFullName() {
		return fullName;
	}
	public void setFullName(String fullName) {
		this.fullName = fullName;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public Year getYearOfPassout() {
		return yearOfPassout;
	}
	public void setYearOfPassout(Year yearOfPassout) {
		this.yearOfPassout = yearOfPassout;
	}
	public Course getCourse() {
		return course;
	}
	public void setCourse(Course course) {
		this.course = course;
	}
	public Branch getBranch() {
		return branch;
	}
	public void setBranch(Branch branch) {
		this.branch = branch;
	}
	public String getEncryptedPassword() {
		return encryptedPassword;
	}
	public void setEncryptedPassword(String encryptedPassword) {
		this.encryptedPassword = encryptedPassword;
	}
	
	
}
