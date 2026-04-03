package com.ietscroll.request;

import org.springframework.web.multipart.MultipartFile;

import com.ietscroll.general.enums.LostItemStatus;

public class LostItemRequest {
	private String lostItemname;
	private MultipartFile imageURLOfItem;
	private String predictedLocation;
	private String description;
	private LostItemStatus status;
	private int prize = 0;

	public String getLostItemname() {
		return lostItemname;
	}

	public void setLostItemname(String lostItemname) {
		this.lostItemname = lostItemname;
	}

	public MultipartFile getImageURLOfItem() {
		return imageURLOfItem;
	}

	public void setImageURLOfItem(MultipartFile imageURLOfItem) {
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

}
