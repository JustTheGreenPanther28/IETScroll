package com.ietscroll.configuration;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.cloudinary.Cloudinary;

@Configuration
public class CloudinaryConfiguration {

	@Value("${cloudinary.cloud-name}")
	private String cloudName;

	@Value("${cloudinary.api-key}")
	private String apiKey;

	@Value("${cloudinary.api-secret}")
	private String apiSecret;

	@Bean
	Cloudinary cloudinary() {
		return new Cloudinary(Map.of(
				"cloud_name", cloudName,
				"api_key", apiKey,
				"api_secret", apiSecret
				));
	}
}
