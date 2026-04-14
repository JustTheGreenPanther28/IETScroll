package com.ietscroll.controller;


import java.util.concurrent.CompletableFuture;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ietscroll.response.QualityOfResume;
import com.ietscroll.service.ResumeCheckerService;

@RestController
@RequestMapping("/api/v1/ietscroll/resume")
public class ResusmeCheckerController {
	
	private ResumeCheckerService resumeCheckerService;
	
	public ResusmeCheckerController(ResumeCheckerService resumeCheckerService) {
		this.resumeCheckerService=resumeCheckerService;
	}
	
	@PostMapping(path="/quality")
	public QualityOfResume getQuality(@RequestPart("file") MultipartFile file, @RequestPart String exp,@RequestPart String role) {
		return resumeCheckerService.getQuality(file,role,Integer.parseInt(exp));
	}
}
