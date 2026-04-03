package com.ietscroll.controller;

import org.modelmapper.ModelMapper;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ietscroll.dto.LostItemDTO;
import com.ietscroll.request.LostItemRequest;
import com.ietscroll.response.Result;
import com.ietscroll.service.LostItemService;

@RestController
@RequestMapping("/api/v1/lost-item")
public class LostItemController {
	
	private LostItemService lostItemService;
	private ModelMapper modelMapper;
	
	public LostItemController(LostItemService lostItemService,ModelMapper modelMapper) {
		this.lostItemService=lostItemService;
		this.modelMapper=modelMapper;
	}
	
	@PostMapping(consumes = {MediaType.APPLICATION_JSON_VALUE,MediaType.MULTIPART_FORM_DATA_VALUE})
	public Result createLostItemPost (Authentication authentication, @RequestPart("data") LostItemRequest lostItemRequest, @RequestPart("image") MultipartFile image) {
		
		return lostItemService.uploadLostItem(modelMapper.map(lostItemRequest, LostItemDTO.class));
	}
}
