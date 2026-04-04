package com.ietscroll.exception;

public class InappropriateImageException extends RuntimeException {
	private static final long serialVersionUID = -1761799238418095213L;

	public InappropriateImageException(String reason) {
		super("Image rejected due to: " + reason);
	}
}