package com.ietscroll.controller;

import org.modelmapper.ModelMapper;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ietscroll.dto.UserDTO;
import com.ietscroll.request.UserRegisterRequest;
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

}
