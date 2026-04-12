package com.ietscroll.service.impl;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.ietscroll.dto.FoundItemDTO;
import com.ietscroll.dto.PagedResponseDTO;
import com.ietscroll.entity.FoundItemEntity;
import com.ietscroll.repository.FoundItemRepository;
import com.ietscroll.response.LostItemResponse;
import com.ietscroll.response.Result;
import com.ietscroll.service.CloudinaryService;
import com.ietscroll.service.FoundItemService;
import com.ietscroll.service.SightEngineService;

@Service
public class FoundItemServiceImpl implements FoundItemService {

	private FoundItemRepository foundItemRepo;
	private CloudinaryService cloudinaryService;
	private SightEngineService sightEngineService;
	private static final List<String> ALLOWED_TYPES = List.of("image/jpeg", "image/png", "image/webp", "image/gif");

	@Override
	public List<FoundItemDTO> getMyFoundItems(String email) {
		List<FoundItemEntity> foundItems = foundItemRepo.findActiveRequestByEmail(email);
		List<FoundItemDTO> foundItemDTOs = new ArrayList<>();

		for (FoundItemEntity foundItem : foundItems) {
			FoundItemDTO lostItemDTO = new FoundItemDTO();
			lostItemDTO.setDescription(foundItem.getDescription());
			lostItemDTO.setImageURL(foundItem.getImageURL());
			lostItemDTO.setPredictedLocation(foundItem.getPredictedLocation());
			lostItemDTO.setFoundItemName(foundItem.getFoundItemName());
			lostItemDTO.setPublicIdOfFoundItem(foundItem.getPublicIdOfFoundItem());
			lostItemDTO.setCreatedAt(foundItem.getCreatedAt());

			foundItemDTOs.add(lostItemDTO);
		}

		return foundItemDTOs;
	}

	@Override
	public Result uploadFoundItem(String email, FoundItemDTO foundItemDTo, MultipartFile image) throws IOException {
		if (foundItemDTo == null) {
			throw new RuntimeException("Please give valid input");
		}

		if (image == null || image.isEmpty()) {
			throw new RuntimeException("Add Image of the lost product!");
		}

		if (!ALLOWED_TYPES.contains(image.getContentType())) {
			throw new RuntimeException("Kindly add valid image type!");
		}

		if (foundItemRepo.countByUser(email) >= 3) {
			throw new RuntimeException(
					"Maximum found-item request of a user can have is three, kindly close other request to create a new request.");
		}

		sightEngineService.checkImage(image);

		// Map has details about uploaded image
		Map uploadedDetail = cloudinaryService.upload(image);
		// Getting url from it
		String url = (String) uploadedDetail.get("secure_url");

		FoundItemEntity foundItem = new FoundItemEntity();
		foundItem.setImageURL(url);
		foundItem.setDescription(foundItemDTo.getDescription());
		foundItem.setFoundItemName(foundItemDTo.getFoundItemName());
		foundItem.setContactTo(email);
		foundItem.setPredictedLocation(foundItemDTo.getPredictedLocation());
		foundItem.setCreatedAt(LocalDateTime.now());

		foundItemRepo.save(foundItem);

		return Result.SUCCUESS;
	}

	@Override
	public PagedResponseDTO<LostItemResponse> getAllFoundItems(int page, int size) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Result closeFoundItemRequest(String email, String publicId) {
		// TODO Auto-generated method stub
		return null;
	}

}
