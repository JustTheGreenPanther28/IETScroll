package com.ietscroll.service;

import com.ietscroll.response.OTPVerificationResult;

public interface OTPService {
	
	void GenerateOTP(String email);
	OTPVerificationResult verifyOTP(int otp,String email);

}
