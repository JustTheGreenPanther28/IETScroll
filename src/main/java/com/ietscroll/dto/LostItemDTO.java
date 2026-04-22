package com.ietscroll.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

import com.ietscroll.general.enums.LostItemStatus;

public class LostItemDTO {

	private int id;
	private UUID publicIdOfLostRequest;
	private String lostItemname;
	private MultipartFile image;
	private String imageURL;
	private String predictedLocation;
	private String description;
	private LostItemStatus status;
	private int prize;
	private String ownerEmail;
	private LocalDateTime createdAt;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public UUID getPublicIdOfLostRequest() {
		return publicIdOfLostRequest;
	}

	public void setPublicIdOfLostRequest(UUID publicIdOfLostRequest) {
		this.publicIdOfLostRequest = publicIdOfLostRequest;
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

	public String getLostItemname() {
		return lostItemname;
	}

	public void setLostItemname(String lostItemname) {
		this.lostItemname = lostItemname;
	}

	public MultipartFile getImage() {
		return image;
	}

	public void setImage(MultipartFile image) {
		this.image = image;
	}

	@Override
	public String toString() {
		return "LostItemDTO [id=" + id + ", publicIdOfLostRequest=" + publicIdOfLostRequest + ", lostItemname="
				+ lostItemname + ", image=" + image + ", immageURL=" + imageURL + ", predictedLocation="
				+ predictedLocation + ", description=" + description + ", status=" + status + ", prize=" + prize
				+ ", ownerEmail=" + ownerEmail + "]";
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public String getImageURL() {
		return imageURL;
	}

	public void setImageURL(String imageURL) {
		this.imageURL = imageURL;
	}

}
