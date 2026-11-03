package com.ietscroll.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ietscroll.dto.UserDTO;
import com.ietscroll.entity.UserEntity;
import com.ietscroll.exception.BadRequestException;
import com.ietscroll.exception.DuplicateResourceException;
import com.ietscroll.exception.ResourceNotFoundException;
import com.ietscroll.general.enums.PrivilegeClass;
import com.ietscroll.repository.UserRepository;
import com.ietscroll.response.Result;
import com.ietscroll.security.Role;
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
	@Value("${server.admin.email}") 
	private String adminEmail;

	public UserServiceImpl(UserRepository userRepo, ModelMapper modelMapper, OTPService otpService,
			BCryptPasswordEncoder encoder) {
		this.userRepo = userRepo;
		this.modelMapper = modelMapper;
		this.otpService = otpService;
		this.encoder = encoder;
	}

	@Override
	public UserDTO register(UserDTO userDTO) {

		if (userDTO.getEmail() == null || !(userDTO.getEmail().endsWith("@ietdavv.edu.in") || userDTO.getEmail().equals(adminEmail))) {
	        throw new BadRequestException("Only @ietdavv.edu.in email addresses are accepted");
	    }
		UserEntity found = userRepo.findByEmail(userDTO.getEmail());
		UserEntity found1 = userRepo.findByUsername(userDTO.getUsername());

		if (found != null || found1 != null) {
			throw new DuplicateResourceException("User with the email/username already exist");
		}

		UserEntity user = new UserEntity();

		user.setEmail(userDTO.getEmail());
		user.setEncryptedPassword(encoder.encode(userDTO.getPassword()));
		user.setUsername(userDTO.getUsername());
		user.setFullName(userDTO.getFullName());
		user.setBranch(userDTO.getBranch());
		user.setCourse(userDTO.getCourse());
		user.setYearOfPassout(userDTO.getYearOfPassout());
		user.setCreatedAt(LocalDateTime.now());
		user.setPrivilegeClass(PrivilegeClass.NEW);

		UserEntity createdUser = userRepo.save(user);
		try {
			otpService.GenerateOTP(user.getEmail());
		} catch (Exception e) {
			// If sending email fails, clean up the pending user so they can retry registering
			userRepo.delete(createdUser);
			throw e;
		}
		return modelMapper.map(createdUser, UserDTO.class);
	}

	@Override
	@Transactional
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
	    if (username == null || !(username.endsWith("@ietdavv.edu.in") || username.equals(adminEmail))) {
	        throw new UsernameNotFoundException("Incorrect email");
	    }
	    UserEntity user = userRepo.findByEmail(username);
	    if (user == null || !user.isVerified()) {
	        throw new UsernameNotFoundException("Incorrect email");
	    }
	    if (user.isBlocked()) {
	        throw new LockedException("Account has been suspended by administrator.");
	    }
	    if (user.getEmail().equalsIgnoreCase(adminEmail) && user.getRole() != Role.SENIOR_ADMIN) {
	        user.setRole(Role.SENIOR_ADMIN);
	        user.setPrivilegeClass(PrivilegeClass.SENIOR_ADMIN);
	        userRepo.save(user);
	    }

	    return new User(
	        user.getEmail(),
	        user.getEncryptedPassword(),
	        List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
	    );
	}

	@Override
	public UserDTO getUserByEmail(@NotNull @Email @NotBlank String email) {
		if (email == null || !(email.endsWith("@ietdavv.edu.in") || email.equals(adminEmail))) {
			throw new BadRequestException("Only @ietdavv.edu.in email addresses are accepted");
		}
		UserEntity user = userRepo.findByEmail(email);
		if (user == null) {
			throw new ResourceNotFoundException("User doesn't exist");
		}
		return modelMapper.map(user, UserDTO.class);
	}

	@Override
	@Transactional
	public Result updateUsername(String email, String newUsername) {
		UserEntity user = userRepo.findByEmail(email);
		var userFound = userRepo.findByUsername(newUsername);

		if (userFound != null) {
			throw new DuplicateResourceException(newUsername + " Already exist, kindly try another username");
		}
		if (user == null) {
			throw new ResourceNotFoundException("User doesn't exist");
		}

		user.setUsername(newUsername);
		userRepo.save(user);

		return Result.SUCCESS;
	}

	@Override
	@Transactional
	public Result updateFullName(String email, String newFullName) {
		UserEntity user = userRepo.findByEmail(email);

		if (user == null) {
			throw new ResourceNotFoundException("User doesn't exist");
		}

		user.setFullName(newFullName);
		userRepo.save(user);

		return Result.SUCCESS;
	}

	@Override
	@Transactional
	public void recordLogin(String email) {
		if (email == null) return;
		UserEntity user = userRepo.findByEmail(email);
		if (user != null) {
			user.setLastLoginAt(LocalDateTime.now());
			if (user.getEmail().equalsIgnoreCase(adminEmail) && user.getRole() != Role.SENIOR_ADMIN) {
				user.setRole(Role.SENIOR_ADMIN);
				user.setPrivilegeClass(PrivilegeClass.SENIOR_ADMIN);
			}
			userRepo.save(user);
		}
	}

	@Override
	@Transactional
	public void recordLogout(String email) {
		if (email == null) return;
		UserEntity user = userRepo.findByEmail(email);
		if (user != null) {
			user.setLastLogoutAt(LocalDateTime.now());
			userRepo.save(user);
		}
	}
}
