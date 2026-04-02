package com.ietscroll.service;

import org.springframework.security.core.userdetails.UserDetailsService;

import com.ietscroll.dto.UserDTO;

public interface UserService extends UserDetailsService
{
	UserDTO register(UserDTO userDTO);
	UserDTO getUserByEmail(String email);
}
