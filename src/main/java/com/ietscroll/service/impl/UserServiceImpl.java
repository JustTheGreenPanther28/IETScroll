package com.ietscroll.service.impl;

//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.ietscroll.dto.UserDTO;
import com.ietscroll.repository.UserRepository;
import com.ietscroll.service.UserService;

@Service
public class UserServiceImpl implements UserService{

	private UserRepository userRepo;
	
	public UserServiceImpl(UserRepository userRepo) {
		this.userRepo=userRepo;
	}
	
	@Override
	public UserDTO register(UserDTO userDTO) {
		
		return null;
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
