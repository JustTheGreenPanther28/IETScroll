package com.ietscroll.service.impl;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.ietscroll.entity.OTPEntity;
import com.ietscroll.entity.UserEntity;
import com.ietscroll.repository.OTPRepository;
import com.ietscroll.repository.UserRepository;
import com.ietscroll.response.Result;
import com.ietscroll.service.EmailService;
import com.ietscroll.service.OTPService;

@Service
public class OTPServiceImpl implements OTPService {

	private final  UserRepository userRepo;
	private final OTPRepository otpRepo;
	private final EmailService emailService;

	public OTPServiceImpl(EmailService emailService, UserRepository userRepo, OTPRepository otpRepo) {
		this.otpRepo = otpRepo;
		this.userRepo = userRepo;
		this.emailService = emailService;
	}

	@Override
	public void GenerateOTP(String email) {
		SecureRandom secureRandom = new SecureRandom();
		int otp = secureRandom.nextInt(100000, 999999);

		OTPEntity otpEntity = new OTPEntity();
		otpEntity.setExpirationTime(LocalDateTime.now().plusMinutes(10));
		otpEntity.setEmail(email);
		otpEntity.setOtp(otp);

		emailService.sendEmail(email, String.valueOf(otp));

		otpRepo.save(otpEntity);

	}

	@Override
	public Result verifyOTP(int otpGivenByUser, String email) {

		otpRepo.deleteOldOTPs();
		List<OTPEntity> otps = otpRepo.findByEmail(email);

		if (otps == null) {
			throw new RuntimeException("Incorrect email or OTP expired!");
		}

		int originalOTP = otps.get(otps.size() - 1).getOtp();

		if (otpGivenByUser == originalOTP) {
			UserEntity user = userRepo.findByEmail(email);
			user.setVerified(true);
			userRepo.save(user);
			return Result.SUCCUESS;
		}

		return Result.FAILED;
	}

}
