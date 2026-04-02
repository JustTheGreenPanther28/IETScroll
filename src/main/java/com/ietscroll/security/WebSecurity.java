
package com.ietscroll.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.ietscroll.service.UserService;

@Configuration
@EnableWebSecurity
public class WebSecurity {

	@Bean
	AuthenticationManager authenticationManager(HttpSecurity http, UserService userService,
			BCryptPasswordEncoder encoder) throws Exception {

		AuthenticationManagerBuilder builder = http.getSharedObject(AuthenticationManagerBuilder.class);

		// It tells Spring how to load users + It tells Spring how to validate passwords
		builder.userDetailsService(userService).passwordEncoder(encoder);

		return builder.build();
	}

	/*
	 * If you actually have any form-based login or cookies, this is a security
	 * hole. 1) authorizeHttpRequests(...): requestMatchers(HttpMethod.POST,
	 * SecurityConstants.SIGN_UP_URL).permitAll() The sign-up endpoint (whatever URL
	 * you defined) is open to everyone. This is the only endpoint that doesn’t
	 * require authentication.
	 * 
	 * 2) .anyRequest().authenticated()
	 * 
	 * Everything else requires a valid login / token. There’s no anonymous access
	 * beyond signup.
	 * 
	 * 3) addFilter(authFilter)
	 * 
	 * You’re plugging in a custom authentication filter (likely something that
	 * handles login requests). This runs at the default position (usually before
	 * username/password filter).
	 * 
	 * 4) addFilterAfter(new AuthorizationFilter(jwtUtil),
	 * AuthenticationFilter.class)
	 * 
	 * You insert your JWT AuthorizationFilter after Spring’s AuthenticationFilter.
	 * 
	 * Purpose: Inspect requests for JWT tokens, verify them, and populate security
	 * context.
	 */
	@Bean
	SecurityFilterChain filterChain(HttpSecurity http, AuthenticationManager authManager, JwtUtil jwtUtil,
			UserService userService) throws Exception {

		AuthenticationFilter authFilter = new AuthenticationFilter(authManager, jwtUtil, userService);

		http.csrf(csrf -> csrf.disable());

		http.cors(Customizer.withDefaults());

		http.authorizeHttpRequests(auth -> auth
				// Swagger (public)
				.requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
				// Public endpoints
				.requestMatchers(HttpMethod.POST, SecurityConstaints.SIGN_UP_URL).permitAll()
				.requestMatchers(HttpMethod.POST, SecurityConstaints.EMAIL_VERIFICATION).permitAll()
				.requestMatchers(HttpMethod.POST, SecurityConstaints.RESEND_OTP).permitAll()
				.requestMatchers(HttpMethod.POST, SecurityConstaints.LOGIN).permitAll()
				.requestMatchers(SecurityConstaints.ADMIN_APIs).hasRole(Role.ADMIN.toString()).anyRequest()
				.authenticated());

		http.addFilter(authFilter).addFilterBefore(new AuthorizationFilter(jwtUtil),
				UsernamePasswordAuthenticationFilter.class);
//		form login is session based , but we are doing token based with JWT
//		http.formLogin(Customizer.withDefaults());

		return http.build();
	}
}
