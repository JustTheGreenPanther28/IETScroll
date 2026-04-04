package com.ietscroll.response;

import java.util.UUID;

public class LostItemResponse {
	private UUID publicIdOfLostRequest;

	private String lostItemname;

	private String imageURLOfItem;

	private String predictedLocation;

	private String description;

	private int prize;

	public UUID getPublicIdOfLostRequest() {
		return publicIdOfLostRequest;
	}

	public void setPublicIdOfLostRequest(UUID publicIdOfLostRequest) {
		this.publicIdOfLostRequest = publicIdOfLostRequest;
	}

	public String getLostItemname() {
		return lostItemname;
	}

	public void setLostItemname(String lostItemname) {
		this.lostItemname = lostItemname;
	}

	public String getImageURLOfItem() {
		return imageURLOfItem;
	}

	public void setImageURLOfItem(String imageURLOfItem) {
		this.imageURLOfItem = imageURLOfItem;
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

	public int getPrize() {
		return prize;
	}

	public void setPrize(int prize) {
		this.prize = prize;
	}

}
