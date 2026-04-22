package com.ietscroll.security;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class AuthorizationFilter extends OncePerRequestFilter {

	private final JwtUtil jwtUtil;

	public AuthorizationFilter(JwtUtil jwtUtil) {
		this.jwtUtil = jwtUtil;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		String path = request.getServletPath();

		if (path.endsWith("/login") || path.startsWith("/v3/api-docs") || path.startsWith("/swagger-ui")) {

			chain.doFilter(request, response);
			return;
		}

		String header = request.getHeader(SecurityConstaints.HEADER_STRING);

		if (header != null && header.startsWith(SecurityConstaints.TOKEN_PREFIX)) {
			String token = header.replace(SecurityConstaints.TOKEN_PREFIX, "");

			try {
				String username = jwtUtil.extractUsername(token);
				String role = jwtUtil.extractRole(token);

				if (username != null && role != null
						&& SecurityContextHolder.getContext().getAuthentication() == null) {
					SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role);
					UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(username, null,
							List.of(authority));
					SecurityContextHolder.getContext().setAuthentication(auth);
				}
			} catch (Exception e) {
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				return;
			}
		}

		chain.doFilter(request, response);
	}
}
