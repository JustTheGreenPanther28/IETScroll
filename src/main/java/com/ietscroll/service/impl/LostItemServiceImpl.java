package com.ietscroll.service.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.ietscroll.dto.LostItemDTO;
import com.ietscroll.dto.PagedResponseDTO;
import com.ietscroll.entity.LostItemEntity;
import com.ietscroll.general.enums.LostItemStatus;
import com.ietscroll.repository.LostItemRepository;
import com.ietscroll.repository.UserRepository;
import com.ietscroll.response.LostItemResponse;
import com.ietscroll.response.Result;
import com.ietscroll.service.CloudinaryService;
import com.ietscroll.service.LostItemService;
import com.ietscroll.service.SightEngineService;

@Service
public class LostItemServiceImpl implements LostItemService {

	private static final List<String> ALLOWED_TYPES = List.of("image/jpeg", "image/png", "image/webp", "image/gif");

	private final LostItemRepository lostItemRepo;
	private final SightEngineService sightEngineService;
	private final CloudinaryService cloudinaryService;

	public LostItemServiceImpl(LostItemRepository lostItemRepo,
			SightEngineService sightEngineService, CloudinaryService cloudinaryService) {
		this.lostItemRepo = lostItemRepo;
		this.sightEngineService = sightEngineService;
		this.cloudinaryService = cloudinaryService;
	}

	@Override
	public Result uploadLostItem(String email, LostItemDTO lostItemDTO, MultipartFile image) throws IOException {

		if (lostItemDTO == null) {
			throw new RuntimeException("Please give valid input");
		}

		if (image == null || image.isEmpty()) {
			throw new RuntimeException("Add Image of the lost product!");
		}

		if (!ALLOWED_TYPES.contains(image.getContentType())) {
			throw new RuntimeException("Kindly add valid image type!");
		}

		if (lostItemRepo.countRequestByUser(email) >= 2) {
			throw new RuntimeException(
					"Maximum lost-item request a user can have is two, kindly close other request to create a new request.");
		}

		sightEngineService.checkImage(image);

		// Map has details about uploaded image
		Map uploadedDetail = cloudinaryService.upload(image);
		// Getting url from it
		String url = (String) uploadedDetail.get("secure_url");

		LostItemEntity lostItem = new LostItemEntity();
		lostItem.setImageURL(url);
		lostItem.setDescription(lostItemDTO.getDescription());
		lostItem.setLostItemname(lostItemDTO.getLostItemname());
		lostItem.setOwnerEmail(email);
		lostItem.setPredictedLocation(lostItemDTO.getPredictedLocation());
		lostItem.setPrize(lostItemDTO.getPrize());
		lostItem.setCreatedAt(LocalDateTime.now());

		lostItemRepo.save(lostItem);

		return Result.SUCCUESS;
	}

	@Override
	public List<LostItemDTO> getMyLostItems(String email) {

		List<LostItemEntity> lostItems = lostItemRepo.findActiveRequestByEmail(email);
		List<LostItemDTO> lostItemsDTOs = new ArrayList<>();

		for (LostItemEntity lostItem : lostItems) {
			LostItemDTO lostItemDTO = new LostItemDTO();
			lostItemDTO.setDescription(lostItem.getDescription());
			lostItemDTO.setImmageURL(lostItem.getImageURL());
			lostItemDTO.setPredictedLocation(lostItem.getPredictedLocation());
			lostItemDTO.setLostItemname(lostItem.getLostItemname());
			lostItemDTO.setPublicIdOfLostRequest(lostItem.getPublicIdOfLostRequest());
			lostItemDTO.setPrize(lostItem.getPrize());
			lostItemDTO.setCreatedAt(lostItem.getCreatedAt());

			lostItemsDTOs.add(lostItemDTO);
		}

		return lostItemsDTOs;
	}

	@Override
	public Result closeLostItem(String email, String publicId) {
		if (email == null || publicId == null) {
			throw new RuntimeException("Invalid credentials");
		}

		// Converting String -> UUID -> Byte Array (Because MYSQL store UUID in form of
		UUID publicUUId = UUID.fromString(publicId);

		ByteBuffer bb = ByteBuffer.allocate(16);
		bb.putLong(publicUUId.getMostSignificantBits());
		bb.putLong(publicUUId.getLeastSignificantBits());

		int rowsChanged = lostItemRepo.closeRequest(email, bb.array());
		if (rowsChanged == 0) {
			return Result.FAILED;
		}
		return Result.SUCCUESS;
	}

	@Override
	public PagedResponseDTO<LostItemResponse> getAllLostItems(int page, int size) {
	    Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
	    Page<LostItemResponse> mapped = lostItemRepo.findByStatus(LostItemStatus.OPEN, pageable)
	            .map(lostItem -> {
	                LostItemResponse response = new LostItemResponse();
	                response.setPublicIdOfLostRequest(lostItem.getPublicIdOfLostRequest());
	                response.setLostItemname(lostItem.getLostItemname());
	                response.setImageURLOfItem(lostItem.getImageURL());
	                response.setPredictedLocation(lostItem.getPredictedLocation());
	                response.setDescription(lostItem.getDescription());
	                response.setPrize(lostItem.getPrize());
	                response.setCreatedAt(lostItem.getCreatedAt());
	                return response;
	            });
	    return PagedResponseDTO.from(mapped);
	}

}
