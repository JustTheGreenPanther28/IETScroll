//package com.ietscroll.security;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//
//import com.ietscroll.service.UserService;
//
////@EnableWebSecurity
////@Configuration
////public class WebSecurity {
//	@Bean
//	AuthenticationManager authenticationManager(HttpSecurity http, UserService userService,
//			BCryptPasswordEncoder encoder) throws Exception {
//
//		AuthenticationManagerBuilder builder = http.getSharedObject(AuthenticationManagerBuilder.class);
//
//		// It tells Spring how to load users + It tells Spring how to validate passwords
//		builder.userDetailsService(userService).passwordEncoder(encoder);
//
//		return builder.build();
//	}
//	
//	
//}
