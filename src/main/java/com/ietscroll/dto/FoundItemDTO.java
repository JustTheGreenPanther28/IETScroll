package com.ietscroll.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

import com.ietscroll.general.enums.FoundItemStatus;

public class FoundItemDTO {
	private int id;

	private UUID publicIdOfFoundItem;

	private String foundItemName;

	private String imageURL;
	
	private MultipartFile image;

	private String predictedLocation;

	private String description;

	private FoundItemStatus status = FoundItemStatus.PENDING;

	private String contactTo;

	private LocalDateTime createdAt;

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

	public MultipartFile getImage() {
		return image;
	}

	public void setImage(MultipartFile image) {
		this.image = image;
	}

}
