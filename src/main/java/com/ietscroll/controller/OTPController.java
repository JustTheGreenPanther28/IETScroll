package com.ietscroll.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ietscroll.request.AccountVerificationRequest;
import com.ietscroll.response.Result;
import com.ietscroll.service.OTPService;

@RestController
@RequestMapping("/api/v1/otp")
public class OTPController {
	
	private OTPService otpService;
	
	public OTPController(OTPService otpService) {
		this.otpService=otpService;
	}
	
	@GetMapping("/verify")
	public Result verifyAccount(@RequestBody AccountVerificationRequest otpAndEmail) {
		return otpService.verifyOTP(otpAndEmail.otp(), otpAndEmail.email());
	}
	
	

}
