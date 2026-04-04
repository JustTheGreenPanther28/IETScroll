package com.ietscroll.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.ietscroll.dto.LostItemDTO;
import com.ietscroll.dto.PagedResponseDTO;
import com.ietscroll.entity.LostItemEntity;
import com.ietscroll.repository.LostItemRepository;
import com.ietscroll.repository.UserRepository;
import com.ietscroll.response.Result;
import com.ietscroll.service.CloudinaryService;
import com.ietscroll.service.LostItemService;
import com.ietscroll.service.SightEngineService;

@Service
public class LostItemServiceImpl implements LostItemService {

	private static final List<String> ALLOWED_TYPES = List.of("image/jpeg", "image/png", "image/webp", "image/gif");

	private final LostItemRepository lostItemRepo;
	private final UserRepository userRepo;
	private final SightEngineService sightEngineService;
	private final CloudinaryService cloudinaryService;

	public LostItemServiceImpl(LostItemRepository lostItemRepo, UserRepository userRepo,
			SightEngineService sightEngineService, CloudinaryService cloudinaryService) {
		this.lostItemRepo = lostItemRepo;
		this.userRepo = userRepo;
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

		System.out.println(lostItemDTO);
		LostItemEntity lostItem = new LostItemEntity();
		lostItem.setImageURL(url);
		lostItem.setDescription(lostItemDTO.getDescription());
		lostItem.setLostItemname(lostItemDTO.getLostItemname());
		lostItem.setOwnerEmail(email);
		lostItem.setPredictedLocation(lostItemDTO.getPredictedLocation());
		lostItem.setPrize(lostItemDTO.getPrize());

		lostItemRepo.save(lostItem);

		return Result.SCCUESS;
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
		}

		return lostItemsDTOs;
	}

	@Override
	public Result closeLostItem(String email, UUID publicId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PagedResponseDTO<LostItemDTO> getAllLostItems(int page, int size, String sortBy, String order) {
		return null;
	}

}
