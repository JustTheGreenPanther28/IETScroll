package com.ietscroll.service;

import com.ietscroll.dto.UserDTO;

public interface UserService //extends UserDetailsService
{
	UserDTO register(UserDTO userDTO);
}
