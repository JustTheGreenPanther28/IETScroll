package com.ietscroll.service.impl;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;

import com.ietscroll.entity.OTPEntity;
import com.ietscroll.entity.UserEntity;
import com.ietscroll.repository.OTPRepository;
import com.ietscroll.repository.UserRepository;
import com.ietscroll.service.OTPService;

public class OTPServiceImpl implements OTPService {

	private UserRepository userRepo;
	private OTPRepository otpRepo;

	public OTPServiceImpl(UserRepository userRepo, OTPRepository otpRepo) {
		this.otpRepo = otpRepo;
		this.userRepo = userRepo;
	}

	@Override
	public void GenerateOTP(String email) {
		SecureRandom secureRandom = new SecureRandom();
		int otp = secureRandom.nextInt(100000, 999999);

		OTPEntity otpEntity = new OTPEntity();
		otpEntity.setExpirationTime(LocalDateTime.now().plusMinutes(10));
		otpEntity.setEmail(email);
		otpEntity.setOtp(otp);

		otpRepo.save(otpEntity);

	}

	@Override
	public boolean verifyOTP(int otpGivenByUser, String email) {
		
		List<OTPEntity> otps =  otpRepo.findByEmail(email);
				
		int originalOTP = otps.get(otps.size()-1).getOtp();
		
		if(otpGivenByUser==originalOTP) {
			UserEntity user = userRepo.findByEmail(email);
			user.setVerified(true);
			userRepo.save(user);
			return true;
		}
		
		return false;
	}

}
