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

		UserDTO userDetailDTO = modelMapper.map(userDetail, UserDTO.class);
		UserDTO createdUserDetailDTO = userService.register(userDetailDTO);
		return new UserResponse(createdUserDetailDTO.getPublicUserId(), createdUserDetailDTO.getUserName(),
				createdUserDetailDTO.getEmail(), createdUserDetailDTO.getUserName(),
				createdUserDetailDTO.getYearOfPassout(), createdUserDetailDTO.getCourse(),
				createdUserDetailDTO.getBranch());
	}

}
