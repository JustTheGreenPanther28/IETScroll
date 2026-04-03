package com.ietscroll.service;

import com.ietscroll.response.Result;

public interface OTPService {
	
	void GenerateOTP(String email);
	Result verifyOTP(int otp,String email);

}
