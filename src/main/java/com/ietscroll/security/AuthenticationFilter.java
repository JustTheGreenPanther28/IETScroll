package com.ietscroll.security;

import java.io.IOException;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ietscroll.request.UserLoginRequest;
import com.ietscroll.service.UserService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class AuthenticationFilter extends UsernamePasswordAuthenticationFilter {
	private final JwtUtil jwtUtil;
	private final UserService userService;

	public AuthenticationFilter(AuthenticationManager authManager, JwtUtil jwtUtil, UserService userService) {
		setAuthenticationManager(authManager);
		setFilterProcessesUrl("/login");
		this.jwtUtil = jwtUtil;
		this.userService = userService;
	}

	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) {

		try {

			UserLoginRequest creds = new ObjectMapper().readValue(request.getInputStream(), UserLoginRequest.class);

			// principle => email , Credentials => password
			return getAuthenticationManager()
					.authenticate(new UsernamePasswordAuthenticationToken(creds.email(), creds.password()));

		} catch (IOException e) {
			throw new RuntimeException("Failed to parse login request", e);
		}
	}

	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,FilterChain chain, Authentication auth) {

		String username = auth.getName();// username <- email
		var user = userService.getUserByEmail(username);// role over here
		String token = jwtUtil.generateToken(username, user.getRole().name());

		response.addHeader(SecurityConstaints.HEADER_STRING, SecurityConstaints.TOKEN_PREFIX + token);
	}
}
