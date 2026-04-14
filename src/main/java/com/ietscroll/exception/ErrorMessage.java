package com.ietscroll.exception;

import java.util.Date;

public class ErrorMessage {
	private Date timestamp;
	private String message;

	public Date getTimestamp() {
		return timestamp;
	}

	public ErrorMessage() {

	}

	public ErrorMessage(Date timestamp, String message) {
		this.timestamp = timestamp;
		this.message = message;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}
