package com.ietscroll.entity;

import java.util.UUID;

import com.ietscroll.general.enums.LostItemStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;

@Entity(name = "LostItem")
public class LostItemEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	@Column(unique = true)
	private UUID publicIdOfLostRequest;

	@Column(nullable = false, length = 25)
	private String lostItemname;

	@Column(columnDefinition = "TEXT", nullable = false, unique = true)
	private String imageURL;

	@Column(nullable = true, length = 50)
	private String predictedLocation;

	@Column(nullable = false, length = 200)
	private String description;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private LostItemStatus status = LostItemStatus.OPEN;

	private int prize = 0;

	@Column(nullable = false)
	private String ownerEmail;

	@PrePersist
	public void onCreate() {
		this.publicIdOfLostRequest = UUID.randomUUID();
	}

	public String getLostItemname() {
		return lostItemname;
	}

	public void setLostItemname(String lostItemname) {
		this.lostItemname = lostItemname;
	}

	public String getImageURL() {
		return imageURL;
	}

	public void setImageURL(String imageURL) {
		this.imageURL = imageURL;
	}

	public String getPredictedLocation() {
		return predictedLocation;
	}

	public void setPredictedLocation(String predictedLocation) {
		this.predictedLocation = predictedLocation;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public LostItemStatus getStatus() {
		return status;
	}

	public void setStatus(LostItemStatus status) {
		this.status = status;
	}

	public int getPrize() {
		return prize;
	}

	public void setPrize(int prize) {
		this.prize = prize;
	}

	public String getOwnerEmail() {
		return ownerEmail;
	}

	public void setOwnerEmail(String ownerEmail) {
		this.ownerEmail = ownerEmail;
	}

	public int getId() {
		return id;
	}

	public UUID getPublicIdOfLostRequest() {
		return publicIdOfLostRequest;
	}

}
