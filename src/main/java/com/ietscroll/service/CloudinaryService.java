package com.ietscroll.service;

import java.io.IOException;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

public interface CloudinaryService {
	
	//It return map, It returns meta data for image uploaded
	@SuppressWarnings("rawtypes")
	Map upload(MultipartFile image) throws IOException;
	
	void deleteImage(String publicId) throws IOException;
}
