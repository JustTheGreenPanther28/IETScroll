package com.ietscroll.security;

import java.io.IOException;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
			String email = creds.email() != null ? creds.email().trim().toLowerCase() : "";
			String password = creds.password() != null ? creds.password() : "";

			// principle => email , Credentials => password
			return getAuthenticationManager()
					.authenticate(new UsernamePasswordAuthenticationToken(email, password));

		} catch (IOException e) {
			throw new RuntimeException("Failed to parse login request", e);
		}
	}

	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication auth) {

		String username = auth.getName(); // username <- email
		try {
			userService.recordLogin(username);
		} catch (Exception e) {
			// Do not fail authentication if login timestamp recording encounters a transient issue
			System.err.println("Note: recordLogin notice: " + e.getMessage());
		}

		String role = "STUDENT";
		if (auth.getAuthorities() != null && !auth.getAuthorities().isEmpty()) {
			String authName = auth.getAuthorities().iterator().next().getAuthority();
			role = authName.replace("ROLE_", "");
		}

		String token = jwtUtil.generateToken(username, role);

		response.addHeader(SecurityConstaints.HEADER_STRING, SecurityConstaints.TOKEN_PREFIX + token);
	}

	@Override
	protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
			org.springframework.security.core.AuthenticationException failed) throws IOException {
		SecurityContextHolder.clearContext();
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");

		String msg = "Invalid email or password.";
		if (failed instanceof org.springframework.security.authentication.LockedException) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			msg = failed.getMessage();
		}

		response.getWriter().write("{\"error\": \"Authentication Failed\", \"message\": \"" + msg + "\"}");
		response.getWriter().flush();
	}
}
