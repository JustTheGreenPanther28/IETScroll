package com.ietscroll.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import com.ietscroll.general.enums.FoundItemStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;

@Entity(name = "FoundItem")
public class FoundItemEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	@Column(unique = true)
	private UUID publicIdOfFoundItem;

	@Column(nullable = false, length = 25)
	private String foundItemName;

	@Column(columnDefinition = "TEXT", nullable = false, unique = true)
	private String imageURL;

	@Column(nullable = true, length = 50)
	private String predictedLocation;

	@Column(nullable = false, length = 200)
	private String description;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private FoundItemStatus status = FoundItemStatus.PENDING;

	@Column(nullable = false)
	private String contactTo;

	private LocalDateTime createdAt = LocalDateTime.now();

	@PrePersist
	public void onCreate() {
		this.publicIdOfFoundItem = UUID.randomUUID();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public UUID getPublicIdOfFoundItem() {
		return publicIdOfFoundItem;
	}

	public void setPublicIdOfFoundItem(UUID publicIdOfFoundItem) {
		this.publicIdOfFoundItem = publicIdOfFoundItem;
	}

	public String getFoundItemName() {
		return foundItemName;
	}

	public void setFoundItemName(String foundItemName) {
		this.foundItemName = foundItemName;
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

	public FoundItemStatus getStatus() {
		return status;
	}

	public void setStatus(FoundItemStatus status) {
		this.status = status;
	}

	public String getContactTo() {
		return contactTo;
	}

	public void setContactTo(String contactTo) {
		this.contactTo = contactTo;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

}
