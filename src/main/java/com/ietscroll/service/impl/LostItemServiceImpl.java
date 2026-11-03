package com.ietscroll.service.impl;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.ietscroll.dto.LostItemDTO;
import com.ietscroll.dto.PagedResponseDTO;
import com.ietscroll.entity.LostItemEntity;
import com.ietscroll.entity.UserEntity;
import com.ietscroll.exception.BadRequestException;
import com.ietscroll.exception.LimitExceededException;
import com.ietscroll.general.enums.LostItemStatus;
import com.ietscroll.general.enums.PrivilegeClass;
import com.ietscroll.repository.LostItemRepository;
import com.ietscroll.repository.UserRepository;
import com.ietscroll.response.LostItemResponse;
import com.ietscroll.response.Result;
import com.ietscroll.service.CloudinaryService;
import com.ietscroll.service.LostItemService;
import com.ietscroll.service.NvidiaVisionService;
import com.ietscroll.service.SightEngineService;
import com.ietscroll.service.SpamProtectionService;
import com.ietscroll.service.UserActivityLogService;

@Service
public class LostItemServiceImpl implements LostItemService {

	private static final List<String> ALLOWED_TYPES = List.of("image/jpeg", "image/png", "image/webp", "image/gif");

	private final LostItemRepository lostItemRepo;
	private final UserRepository userRepo;
	private final SightEngineService sightEngineService;
	private final CloudinaryService cloudinaryService;
	private final SpamProtectionService spamProtectionService;
	private final NvidiaVisionService nvidiaVisionService;
	private final UserActivityLogService activityLogService;
	private final Executor moderationExecutor;

	public LostItemServiceImpl(
			LostItemRepository lostItemRepo,
			UserRepository userRepo,
			SightEngineService sightEngineService,
			CloudinaryService cloudinaryService,
			SpamProtectionService spamProtectionService,
			NvidiaVisionService nvidiaVisionService,
			UserActivityLogService activityLogService,
			@Qualifier("moderationExecutor") Executor moderationExecutor) {
		this.lostItemRepo = lostItemRepo;
		this.userRepo = userRepo;
		this.sightEngineService = sightEngineService;
		this.cloudinaryService = cloudinaryService;
		this.spamProtectionService = spamProtectionService;
		this.nvidiaVisionService = nvidiaVisionService;
		this.activityLogService = activityLogService;
		this.moderationExecutor = moderationExecutor;
	}

	@Override
	@Transactional
	public Result uploadLostItem(String email, LostItemDTO lostItemDTO, MultipartFile image) throws IOException {

		if (lostItemDTO == null) {
			throw new BadRequestException("Please give valid input");
		}

		String itemName = lostItemDTO.getLostItemname() != null ? lostItemDTO.getLostItemname().trim() : "";
		String description = lostItemDTO.getDescription() != null ? lostItemDTO.getDescription().trim() : "";
		String location = lostItemDTO.getPredictedLocation() != null ? lostItemDTO.getPredictedLocation().trim() : "";

		// Campus location policy: reports must be within or near college campus.
		if (location.isBlank()) {
			UserEntity owner = userRepo.findByEmail(email);
			if (owner != null && owner.getRole() == com.ietscroll.security.Role.STUDENT) {
				owner.setPrivilegeClass(PrivilegeClass.MAY_BE_SPAM);
				userRepo.save(owner);
			}
			activityLogService.logActivity(email, "LOST", itemName, "REJECTED_BY_MODERATION", "Missing campus location (marked as MAY_BE_SPAM)");
			throw new BadRequestException("Item location must be specified and near the college campus only (e.g. Library, Canteen, Block A, Workshop). Reports without location are treated as SPAM.");
		}

		if (itemName.length() < 3) {
			throw new BadRequestException("Please provide a valid item name (at least 3 characters).");
		}

		if (description.length() < 20) {
			throw new BadRequestException("Please provide a detailed description (at least 20 characters) describing the item's appearance, color, brand, or condition.");
		}

		if (image == null || image.isEmpty()) {
			throw new BadRequestException("Add Image of the lost product!");
		}

		if (!ALLOWED_TYPES.contains(image.getContentType())) {
			throw new BadRequestException("Kindly add valid image type!");
		}

		if (lostItemRepo.countRequestByUser(email) >= 2) {
			throw new LimitExceededException(
					"Maximum lost-item request a user can have is two, kindly close other request to create a new request.");
		}

		try {
			// ── STEP 1 & 2: Run Text and Vision verification concurrently using dedicated bounded executor ──
			CompletableFuture<Void> textFuture = CompletableFuture.runAsync(() ->
					spamProtectionService.checkItemSubmission(itemName, description, location),
					moderationExecutor
			);

			CompletableFuture<Void> visionFuture = CompletableFuture.runAsync(() -> {
				try {
					nvidiaVisionService.checkImageRelevance(image, itemName, description);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}, moderationExecutor);

			CompletableFuture.allOf(textFuture, visionFuture).join();
		} catch (CompletionException ce) {
			Throwable cause = ce.getCause();
			String errorMsg = cause != null ? cause.getMessage() : "Content moderation check failed";
			activityLogService.logActivity(email, "LOST", itemName, "REJECTED_BY_MODERATION", errorMsg);
			if (cause instanceof RuntimeException re) throw re;
			if (cause instanceof IOException io) throw io;
			throw new RuntimeException(cause);
		}

		// ── STEP 3: SightEngine double-verification ────────────
		try {
			sightEngineService.checkImage(image);
		} catch (Exception e) {
			activityLogService.logActivity(email, "LOST", itemName, "REJECTED_BY_MODERATION", e.getMessage());
			throw e;
		}

		// Map has details about uploaded image
		Map<?, ?> uploadedDetail = cloudinaryService.upload(image);
		String url = (String) uploadedDetail.get("secure_url");

		LostItemEntity lostItem = new LostItemEntity();
		lostItem.setImageURL(url);
		lostItem.setDescription(description);
		lostItem.setLostItemname(itemName);
		lostItem.setOwnerEmail(email);
		lostItem.setPredictedLocation(location);
		lostItem.setPrize(lostItemDTO.getPrize());
		lostItem.setCreatedAt(LocalDateTime.now());

		lostItemRepo.save(lostItem);

		activityLogService.logActivity(email, "LOST", itemName, "UPLOADED", "Item successfully verified and listed publicly. Location: " + location);

		return Result.SUCCESS;
	}

	@Override
	public List<LostItemDTO> getMyLostItems(String email) {

		List<LostItemEntity> lostItems = lostItemRepo.findActiveRequestByEmail(email);
		List<LostItemDTO> lostItemsDTOs = new ArrayList<>();

		for (LostItemEntity lostItem : lostItems) {
			LostItemDTO lostItemDTO = new LostItemDTO();
			lostItemDTO.setDescription(lostItem.getDescription());
			lostItemDTO.setImageURL(lostItem.getImageURL());
			lostItemDTO.setPredictedLocation(lostItem.getPredictedLocation());
			lostItemDTO.setLostItemname(lostItem.getLostItemname());
			lostItemDTO.setPublicIdOfLostRequest(lostItem.getPublicIdOfLostRequest());
			lostItemDTO.setPrize(lostItem.getPrize());
			lostItemDTO.setCreatedAt(lostItem.getCreatedAt());
			lostItemDTO.setOwnerEmail(lostItem.getOwnerEmail());

			lostItemsDTOs.add(lostItemDTO);
		}

		return lostItemsDTOs;
	}

	@Override
	@Transactional
	public Result closeLostItem(String email, String publicId) {
		if (email == null || publicId == null) {
			throw new BadRequestException("Invalid credentials");
		}

		UUID publicUUID = UUID.fromString(publicId);

		int rowsChanged = lostItemRepo.closeRequest(email, publicUUID);
		if (rowsChanged == 0) {
			return Result.FAILED;
		}
		return Result.SUCCESS;
	}

	@Override
	public PagedResponseDTO<LostItemResponse> getAllLostItems(int page, int size) {
		Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
		Page<LostItemResponse> mapped = lostItemRepo.findByStatus(LostItemStatus.OPEN, pageable).map(lostItem -> {
			LostItemResponse response = new LostItemResponse();
			response.setPublicIdOfLostRequest(lostItem.getPublicIdOfLostRequest());
			response.setLostItemname(lostItem.getLostItemname());
			response.setImageURLOfItem(lostItem.getImageURL());
			response.setPredictedLocation(lostItem.getPredictedLocation());
			response.setDescription(lostItem.getDescription());
			response.setPrize(lostItem.getPrize());
			response.setCreatedAt(lostItem.getCreatedAt());
			response.setContactTo(lostItem.getOwnerEmail());

			UserEntity owner = userRepo.findByEmail(lostItem.getOwnerEmail());
			if (owner != null && owner.getPrivilegeClass() != null) {
				response.setOwnerPrivilege(owner.getPrivilegeClass().name());
			} else {
				response.setOwnerPrivilege("NORMAL");
			}

			return response;
		});
		return PagedResponseDTO.from(mapped);
	}
}
