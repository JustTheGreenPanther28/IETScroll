package com.ietscroll.service.impl;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.ietscroll.dto.SightEngineResponse;
import com.ietscroll.service.SightEngineService;
import com.ietscroll.util.ModerationEvaluator;

@Service
public class SightEngineServiceImpl implements SightEngineService {
	@Value("${sightengine.api-user}")
	private String apiUser;

	@Value("${sightengine.api-secret}")
	private String apiSecret;

	@Value("${sightengine.api-url}")
	private String apiUrl;

	private final RestTemplate restTemplate = new RestTemplate();
	private final ModerationEvaluator evaluator;

	public SightEngineServiceImpl(ModerationEvaluator evaluator) {
		this.evaluator = evaluator;
	}

	public void checkImage(MultipartFile file) throws IOException {
		LinkedMultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
		body.add("media", new ByteArrayResource(file.getBytes()) {
			@Override
			public String getFilename() {
				return file.getOriginalFilename();
			}
		});
		body.add("models", "nudity-2.1,weapon,alcohol,recreational_drug,gore-2.0,violence,self-harm,offensive-2.0");
		body.add("api_user", apiUser);
		body.add("api_secret", apiSecret);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);

		SightEngineResponse response = restTemplate
				.postForEntity(apiUrl, new HttpEntity<>(body, headers), SightEngineResponse.class).getBody();

		evaluator.evaluate(response);
	}
}
