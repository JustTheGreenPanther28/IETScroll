package com.ietscroll.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ietscroll.response.QualityOfResume;
import com.ietscroll.service.ResumeCheckerService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/ietscroll/resume")
@Tag(name = "Resume Analysis", description = "AI-powered resume evaluation based on role and experience.")
public class ResumeCheckerController {

	private final ResumeCheckerService resumeCheckerService;

	public ResumeCheckerController(ResumeCheckerService resumeCheckerService) {
		this.resumeCheckerService = resumeCheckerService;
	}

	@PostMapping(path = "/quality")
	@Operation(summary = "Analyze resume quality", description = "Evaluates resume using AI (LLM). Limited to 3 times per month per student.")
	public ResponseEntity<QualityOfResume> getQuality(
			Authentication auth,
			@RequestPart("file") MultipartFile file,
			@RequestPart String exp,
			@RequestPart String role) {
		String userEmail = auth != null ? auth.getName() : null;
		return ResponseEntity.status(HttpStatus.ACCEPTED)
				.body(resumeCheckerService.getQualityForUser(userEmail, file, role, Integer.parseInt(exp)));
	}
}
