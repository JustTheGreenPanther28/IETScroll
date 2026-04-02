package com.ietscroll.entity;

import java.time.Year;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.validation.constraints.Email;

@Entity(name = "user")
public class UserEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int userId;

	@Column(nullable = false, unique = true)
	private UUID publicUserId;

	@Column(nullable = false, unique = true, length = 35)
	private String userName;

	@Column(nullable = true, unique = false, length = 55)
	private String fullName;

	@Email
	@Column(nullable = false, length = 50)
	private String email;

	@Column(nullable = true)
	private Year yearOfPassout;

	@Column(nullable = false)
	private Course course;

	@Column(nullable = false)
	private Branch branch;

	@Column(nullable = false)
	private String encryptedPassword;

	@PrePersist
	protected void onCreate() {
		this.publicUserId = UUID.randomUUID();
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public UUID getPublicUserId() {
		return publicUserId;
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
