package com.ietscroll.controller;

import org.modelmapper.ModelMapper;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ietscroll.dto.UserDTO;
import com.ietscroll.request.UserRegisterRequest;
import com.ietscroll.response.Result;
import com.ietscroll.response.UserResponse;
import com.ietscroll.service.UserService;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {

	private UserService userService;
	private ModelMapper modelMapper;

	public UserController(UserService userService, ModelMapper modelMapper) {
		this.userService = userService;
		this.modelMapper = modelMapper;
	}

	@PostMapping("/register")
	public UserResponse register(@RequestBody UserRegisterRequest userDetail) {

		UserDTO userDetailDTO = new UserDTO();
		userDetailDTO.setBranch(userDetail.branch());
		userDetailDTO.setCourse(userDetail.course());
		userDetailDTO.setEmail(userDetail.email());
		userDetailDTO.setFullName(userDetail.fullName());
		userDetailDTO.setPassword(userDetail.password());
		userDetailDTO.setUsername(userDetail.username());
 		userDetailDTO.setYearOfPassout(userDetail.yearOfPassout());
		UserDTO createdUserDetailDTO = userService.register(userDetailDTO);
		return new UserResponse(createdUserDetailDTO.getPublicUserId(), createdUserDetailDTO.getUsername(),
				createdUserDetailDTO.getEmail(), createdUserDetailDTO.getFullName(), createdUserDetailDTO.getYearOfPassout(),
				createdUserDetailDTO.getCourse(), createdUserDetailDTO.getBranch());
	}
	
	@GetMapping
	public UserResponse getUser(Authentication authentication) {
		UserDTO userDTO =  userService.getUserByEmail(authentication.getName());
		return new UserResponse(userDTO.getPublicUserId(),userDTO.getUsername(),userDTO.getEmail(),userDTO.getFullName(),userDTO.getYearOfPassout(),userDTO.getCourse(),userDTO.getBranch());
	}
	
	@PatchMapping("/username/{newUsername}")
	public Result updateUsername(Authentication authentication, @PathVariable String newUsername) {
		return userService.updateUsername(authentication.getName(), newUsername);
	}
	
	@PatchMapping("/fullname/{fullname}")
	public Result updateFullname(Authentication authentication, @PathVariable String fullname) {
		return userService.updateFullName(authentication.getName(), fullname);
	}

}
