package com.ietscroll.service;

import java.util.concurrent.CompletableFuture;

import org.springframework.web.multipart.MultipartFile;

import com.ietscroll.response.QualityOfResume;

public interface ResumeCheckerService {

	QualityOfResume getQuality(MultipartFile file,String roles,int experience);
	 
}
