package com.ietscroll.service.impl;

import org.modelmapper.ModelMapper;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.ietscroll.dto.UserDTO;
import com.ietscroll.entity.UserEntity;
import com.ietscroll.repository.UserRepository;
import com.ietscroll.service.OTPService;
import com.ietscroll.service.UserService;

@Service
public class UserServiceImpl implements UserService {

	private UserRepository userRepo;
	private OTPService otpService;
	private ModelMapper modelMapper;

	public UserServiceImpl(UserRepository userRepo, ModelMapper modelMapper, OTPService otpService) {
		this.userRepo = userRepo;
		this.modelMapper = modelMapper;
		this.otpService = otpService;
	}

	@Override
	public UserDTO register(UserDTO userDTO) {

		if (!userDTO.getEmail().endsWith(CollegeDefaults.COLLEGE_EMAIL_ENDS_WITH.name())) {
			throw new RuntimeException("Kindly enter college email");
		}
		UserEntity found = userRepo.findByEmail(userDTO.getEmail());
		UserEntity found1 = userRepo.findByUsername(userDTO.getUserName());

		if (found != null || found1 != null) {
			throw new RuntimeException("User with the email/username already exist");
		}

		UserEntity user = new UserEntity();

		user.setEmail(userDTO.getEmail());
		user.setEncryptedPassword(userDTO.getEncryptedPassword());
		user.setUserName(userDTO.getUserName());
		user.setFullName(userDTO.getFullName());
		user.setBranch(userDTO.getBranch());
		user.setCourse(userDTO.getCourse());
		user.setYearOfPassout(userDTO.getYearOfPassout());

		user = userRepo.save(user);

		otpService.GenerateOTP(user.getEmail());
		return modelMapper.map(user, UserDTO.class);
	}

//	@Override
//	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
//		
//		if(username.endsWith(CollegeDefaults.COLLEGE_EMAIL_ENDS_WITH.name())) {
//			new UsernameNotFoundException("Enter IET DAVV college email");
//		}
//		
//		
//		
//		return null;
//	}

}
