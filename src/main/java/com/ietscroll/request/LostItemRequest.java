package com.ietscroll.request;

import org.springframework.web.multipart.MultipartFile;

import com.ietscroll.general.enums.LostItemStatus;

public class LostItemRequest {
	private String lostItemname;
	private String predictedLocation;
	private String description;
	private int prize;

	public String getLostItemname() {
		return lostItemname;
	}

	public void setLostItemname(String lostItemname) {
		this.lostItemname = lostItemname;
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
