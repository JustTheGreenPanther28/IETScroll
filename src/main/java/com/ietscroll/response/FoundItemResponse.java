package com.ietscroll.response;

import java.time.LocalDateTime;
import java.util.UUID;

public class FoundItemResponse {
	private UUID publicIdOfFoundItem;

	private String foundItemName;

	private String imageURL;

	private String predictedLocation;

	private String description;

	private String contactTo;

	private LocalDateTime createdAt;

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
