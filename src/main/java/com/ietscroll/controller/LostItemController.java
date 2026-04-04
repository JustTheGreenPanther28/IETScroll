package com.ietscroll.controller;

import java.io.IOException;

import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ietscroll.dto.LostItemDTO;
import com.ietscroll.request.LostItemRequest;
import com.ietscroll.response.Result;
import com.ietscroll.service.LostItemService;

@RestController
@RequestMapping("/api/v1/lost-item")
public class LostItemController {

	private LostItemService lostItemService;
	private ModelMapper modelMapper;

	public LostItemController(LostItemService lostItemService, ModelMapper modelMapper) {
		this.lostItemService = lostItemService;
		this.modelMapper = modelMapper;
	}

	@PostMapping(consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
	public ResponseEntity<Result> createLostItemPost(@RequestPart("data") String lostItemJSON,
			@RequestPart("image") MultipartFile image) throws IOException {
		
		ObjectMapper mapper = new ObjectMapper();
	    LostItemRequest lostItemRequest = mapper.readValue(lostItemJSON, LostItemRequest.class);
	    
		System.out.println("hereeeeeeeeeeeeee");
		Result result = lostItemService.uploadLostItem(modelMapper.map(lostItemRequest, LostItemDTO.class), image);
		System.out.println("hereeeee");

		ResponseEntity<Result> finalResult = new ResponseEntity<>(result, HttpStatusCode.valueOf(200));
		return finalResult;
	}
}
