package com.ietscroll.controller;


import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
	
	@PostMapping(path="/quality" , consumes= {MediaType.MULTIPART_FORM_DATA_VALUE})
	public QualityOfResume getQuality(@RequestPart("file") MultipartFile file, @RequestParam int exp,@RequestParam String role) {
		return resumeCheckerService.getQuality(file,role,exp);
	}
}
