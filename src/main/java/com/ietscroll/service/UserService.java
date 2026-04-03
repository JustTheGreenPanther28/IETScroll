package com.ietscroll.service;

import org.springframework.security.core.userdetails.UserDetailsService;

import com.ietscroll.dto.UserDTO;
import com.ietscroll.response.Result;

public interface UserService extends UserDetailsService
{
	UserDTO register(UserDTO userDTO);
	UserDTO getUserByEmail(String email);
	Result updateUsername(String email,String newUsername);
	Result updateFullName(String email,String newFullName);
}
