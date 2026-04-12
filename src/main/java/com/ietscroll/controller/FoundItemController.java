package com.ietscroll.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ietscroll.dto.FoundItemDTO;
import com.ietscroll.dto.PagedResponseDTO;
import com.ietscroll.request.FoundItemRequest;
import com.ietscroll.response.FoundItemResponse;
import com.ietscroll.response.Result;
import com.ietscroll.service.FoundItemService;

@RestController
@RequestMapping("/api/v1/found-item")
public class FoundItemController {

	private FoundItemService foundItemService;

	public FoundItemController(FoundItemService foundItemService, ModelMapper modelMapper) {
		this.foundItemService = foundItemService;
	}

	@PostMapping(consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
	public ResponseEntity<Result> createLostItemPost(Authentication authentication,
			@RequestPart("data") String foundItemJSON, @RequestPart("image") MultipartFile image) throws IOException {

		ObjectMapper mapper = new ObjectMapper();
		FoundItemRequest foundItemRequest = mapper.readValue(foundItemJSON, FoundItemRequest.class);

		FoundItemDTO foundItemDTO = new FoundItemDTO();

		foundItemDTO.setDescription(foundItemRequest.description());
		foundItemDTO.setFoundItemName(foundItemRequest.foundItemname());
		foundItemDTO.setPredictedLocation(foundItemRequest.predictedLocation());
		foundItemDTO.setContactTo(authentication.getName());

		Result result = foundItemService.uploadFoundItem(authentication.getName(), foundItemDTO, image);

		ResponseEntity<Result> finalResult = new ResponseEntity<>(result, HttpStatusCode.valueOf(200));
		return finalResult;
	}

	@GetMapping("/me")
	public List<FoundItemResponse> getUserLostItem(Authentication authentication) {
		List<FoundItemDTO> foundItemDTOs = foundItemService.getMyFoundItems(authentication.getName());

		List<FoundItemResponse> foundItemResponses = new ArrayList<>();
		for (FoundItemDTO foundItemDTO : foundItemDTOs) {
			FoundItemResponse foundItemResponse = new FoundItemResponse();
			foundItemResponse.setPublicIdOfFoundItem(foundItemDTO.getPublicIdOfFoundItem());
			foundItemResponse.setFoundItemName(foundItemDTO.getFoundItemName());
			foundItemResponse.setDescription(foundItemDTO.getDescription());
			foundItemResponse.setImageURL(foundItemDTO.getImageURL());
			foundItemResponse.setPredictedLocation(foundItemDTO.getPredictedLocation());
			foundItemResponse.setContactTo(foundItemDTO.getContactTo());
			foundItemResponses.add(foundItemResponse);
		}
		return foundItemResponses;
	}

	@PatchMapping("/close")
	public Result closeLostItemRequest(Authentication authentication, @RequestParam String foundItemId) {
		return foundItemService.closeFoundItemRequest(authentication.getName(), foundItemId);
	}

	@GetMapping
	public ResponseEntity<PagedResponseDTO<FoundItemResponse>> getAllLostItems(
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
		return ResponseEntity.ok(foundItemService.getAllFoundItems(page, size));
	}
}
