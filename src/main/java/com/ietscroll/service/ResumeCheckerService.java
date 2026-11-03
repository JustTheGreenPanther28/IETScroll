package com.ietscroll.service;

import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

import com.ietscroll.response.QualityOfResume;

public interface ResumeCheckerService {
	QualityOfResume getQuality(MultipartFile file, String role, int exp);
	QualityOfResume getQualityForUser(String userEmail, MultipartFile file, String role, int exp);
}
