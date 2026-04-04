package com.ietscroll.service.impl;

import java.util.ArrayList;

import org.modelmapper.ModelMapper;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.ietscroll.dto.UserDTO;
import com.ietscroll.entity.UserEntity;
import com.ietscroll.repository.UserRepository;
import com.ietscroll.response.Result;
import com.ietscroll.service.OTPService;
import com.ietscroll.service.UserService;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Service
public class UserServiceImpl implements UserService {

	private final UserRepository userRepo;
	private final OTPService otpService;
	private final ModelMapper modelMapper;
	private final BCryptPasswordEncoder encoder;

	public UserServiceImpl(UserRepository userRepo, ModelMapper modelMapper, OTPService otpService,
			BCryptPasswordEncoder encoder) {
		this.userRepo = userRepo;
		this.modelMapper = modelMapper;
		this.otpService = otpService;
		this.encoder = encoder;
	}

	@Override
	public UserDTO register(UserDTO userDTO) {

		if (!userDTO.getEmail().endsWith("@ietdavv.edu.in")) {
			throw new RuntimeException("Kindly enter college email");
		}
		UserEntity found = userRepo.findByEmail(userDTO.getEmail());
		UserEntity found1 = userRepo.findByUsername(userDTO.getUsername());

		if (found != null || found1 != null) {
			throw new RuntimeException("User with the email/username already exist");
		}

		UserEntity user = new UserEntity();

		user.setEmail(userDTO.getEmail());
		user.setEncryptedPassword(encoder.encode(userDTO.getPassword()));
		user.setUsername(userDTO.getUsername());
		user.setFullName(userDTO.getFullName());
		user.setBranch(userDTO.getBranch());
		user.setCourse(userDTO.getCourse());
		user.setYearOfPassout(userDTO.getYearOfPassout());

		UserEntity createdUser = userRepo.save(user);
		otpService.GenerateOTP(user.getEmail());
		return modelMapper.map(createdUser, UserDTO.class);
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

		if (username == null || !username.endsWith("@ietdavv.edu.in")) {
			throw new UsernameNotFoundException("Incorrect email");
		}
		UserEntity user = userRepo.findByEmail(username);
		if (user == null || !user.isVerified()) {
			throw new UsernameNotFoundException("Inccorect email");
		}

		return new User(user.getEmail(), user.getEncryptedPassword(), new ArrayList<>());
	}

	@Override
	public UserDTO getUserByEmail(@NotNull @Email @NotBlank String email) {
		if (email == null || !email.endsWith("@ietdavv.edu.in")) {
			throw new UsernameNotFoundException("Incorrect email");
		}
		UserEntity user = userRepo.findByEmail(email);
		if (user == null) {
			throw new UsernameNotFoundException("User doesn't exist");
		}
		return modelMapper.map(user, UserDTO.class);
	}

	@Override
	public Result updateUsername(String email, String newUsername) {
		UserEntity user = userRepo.findByEmail(email);
		var userFound = userRepo.findByUsername(newUsername);

		if (userFound != null) {
			throw new RuntimeException(newUsername + " Already exist, kindly try another username");
		}
		if (user == null) {
			throw new UsernameNotFoundException("User doesn't exist");
		}

		user.setUsername(newUsername);
		userRepo.save(user);

		return Result.SUCCUESS;
	}

	@Override
	public Result updateFullName(String email, String newFullName) {
		UserEntity user = userRepo.findByEmail(email);

		if (user == null) {
			throw new UsernameNotFoundException("User doesn't exist");
		}

		user.setFullName(newFullName);
		userRepo.save(user);

		return Result.SUCCUESS;
	}

}
