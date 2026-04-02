package com.ietscroll.service;

public interface OTPService {
	
	void GenerateOTP(String email);
	boolean verifyOTP(int otp,String email);

}
