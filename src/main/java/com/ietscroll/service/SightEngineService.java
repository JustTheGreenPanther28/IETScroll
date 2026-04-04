package com.ietscroll.service;

import java.io.IOException;

import org.springframework.web.multipart.MultipartFile;

public interface SightEngineService {
	void checkImage(MultipartFile image) throws IOException;
}
