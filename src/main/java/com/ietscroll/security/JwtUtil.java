package com.ietscroll.security;

import java.time.Instant;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {

	private final SecretKey key;

	public JwtUtil(@Value("${jwt.secret}") String jwtSecret) {
		// Use ONE single source of truth for the secret
		this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
	}

	public String generateToken(String username, String role) {

		Instant now = Instant.now();

		return Jwts.builder().subject(username).claim("role", role).issuedAt(Date.from(now))
				.expiration(Date.from(now.plusMillis(SecurityConstaints.EXPIRATION_TIME))).signWith(key).compact();
	}

	public String extractUsername(String token) {

		return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().getSubject();
	}

	public String extractRole(String token) {
		return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().get("role", String.class);
	}
}
