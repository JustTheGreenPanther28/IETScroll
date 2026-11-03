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

import com.ietscroll.dto.FoundItemDTO;
import com.ietscroll.dto.PagedResponseDTO;
import com.ietscroll.entity.FoundItemEntity;
import com.ietscroll.entity.UserEntity;
import com.ietscroll.exception.BadRequestException;
import com.ietscroll.exception.LimitExceededException;
import com.ietscroll.general.enums.FoundItemStatus;
import com.ietscroll.general.enums.PrivilegeClass;
import com.ietscroll.repository.FoundItemRepository;
import com.ietscroll.repository.UserRepository;
import com.ietscroll.response.FoundItemResponse;
import com.ietscroll.response.Result;
import com.ietscroll.service.CloudinaryService;
import com.ietscroll.service.FoundItemService;
import com.ietscroll.service.NvidiaVisionService;
import com.ietscroll.service.SightEngineService;
import com.ietscroll.service.SpamProtectionService;
import com.ietscroll.service.UserActivityLogService;

@Service
public class FoundItemServiceImpl implements FoundItemService {

	private final FoundItemRepository foundItemRepo;
	private final UserRepository userRepo;
	private final CloudinaryService cloudinaryService;
	private final SightEngineService sightEngineService;
	private final SpamProtectionService spamProtectionService;
	private final NvidiaVisionService nvidiaVisionService;
	private final UserActivityLogService activityLogService;
	private final Executor moderationExecutor;

	private static final List<String> ALLOWED_TYPES = List.of("image/jpeg", "image/png", "image/webp", "image/gif");

	public FoundItemServiceImpl(
			FoundItemRepository foundItemRepo,
			UserRepository userRepo,
			CloudinaryService cloudinaryService,
			SightEngineService sightEngineService,
			SpamProtectionService spamProtectionService,
			NvidiaVisionService nvidiaVisionService,
			UserActivityLogService activityLogService,
			@Qualifier("moderationExecutor") Executor moderationExecutor) {
		this.foundItemRepo = foundItemRepo;
		this.userRepo = userRepo;
		this.cloudinaryService = cloudinaryService;
		this.sightEngineService = sightEngineService;
		this.spamProtectionService = spamProtectionService;
		this.nvidiaVisionService = nvidiaVisionService;
		this.activityLogService = activityLogService;
		this.moderationExecutor = moderationExecutor;
	}

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
			lostItemDTO.setContactTo(email);

			foundItemDTOs.add(lostItemDTO);
		}

		return foundItemDTOs;
	}

	@Override
	@Transactional
	public Result uploadFoundItem(String email, FoundItemDTO foundItemDTo, MultipartFile image) throws IOException {
		if (foundItemDTo == null) {
			throw new BadRequestException("Please give valid input");
		}

		String itemName = foundItemDTo.getFoundItemName() != null ? foundItemDTo.getFoundItemName().trim() : "";
		String description = foundItemDTo.getDescription() != null ? foundItemDTo.getDescription().trim() : "";
		String location = foundItemDTo.getPredictedLocation() != null ? foundItemDTo.getPredictedLocation().trim() : "";

		// Campus location policy: reports must be within or near college campus.
		if (location.isBlank()) {
			UserEntity reporter = userRepo.findByEmail(email);
			if (reporter != null && reporter.getPrivilegeClass() != PrivilegeClass.ADMIN) {
				reporter.setPrivilegeClass(PrivilegeClass.MAY_BE_SPAM);
				userRepo.save(reporter);
			}
			activityLogService.logActivity(email, "FOUND", itemName, "REJECTED_BY_MODERATION", "Missing campus location (marked as MAY_BE_SPAM)");
			throw new BadRequestException("Found item location must be specified and near the college campus only (e.g. Workshop, Library, Canteen, Block B). Reports without location are treated as SPAM.");
		}

		if (itemName.length() < 3) {
			throw new BadRequestException("Please provide a valid item name (at least 3 characters).");
		}

		if (description.length() < 20) {
			throw new BadRequestException("Please provide a detailed description (at least 20 characters) describing the item's appearance, color, brand, or condition.");
		}

		if (image == null || image.isEmpty()) {
			throw new BadRequestException("Add Image of the found product!");
		}

		if (!ALLOWED_TYPES.contains(image.getContentType())) {
			throw new BadRequestException("Kindly add valid image type!");
		}

		if (foundItemRepo.countByUser(email) >= 3) {
			throw new LimitExceededException(
					"Maximum found-item request of a user can have is three, kindly close other request to create a new request.");
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
			activityLogService.logActivity(email, "FOUND", itemName, "REJECTED_BY_MODERATION", errorMsg);
			if (cause instanceof RuntimeException re) throw re;
			if (cause instanceof IOException io) throw io;
			throw new RuntimeException(cause);
		}

		// ── STEP 3: SightEngine double-verification ────────────
		try {
			sightEngineService.checkImage(image);
		} catch (Exception e) {
			activityLogService.logActivity(email, "FOUND", itemName, "REJECTED_BY_MODERATION", e.getMessage());
			throw e;
		}

		Map<?, ?> upload = cloudinaryService.upload(image);
		String imageURL = (String) upload.get("secure_url");

		FoundItemEntity foundItemEntity = new FoundItemEntity();
		foundItemEntity.setContactTo(email);
		foundItemEntity.setDescription(description);
		foundItemEntity.setFoundItemName(itemName);
		foundItemEntity.setImageURL(imageURL);
		foundItemEntity.setPredictedLocation(location);
		foundItemEntity.setCreatedAt(LocalDateTime.now());

		foundItemRepo.save(foundItemEntity);

		activityLogService.logActivity(email, "FOUND", itemName, "UPLOADED", "Item successfully verified and listed publicly. Location: " + location);

		return Result.SUCCESS;
	}

	@Override
	public PagedResponseDTO<FoundItemResponse> getAllFoundItems(int page, int size) {
		Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

		Page<FoundItemResponse> mapped = foundItemRepo.findByStatus(FoundItemStatus.PENDING, pageable)
				.map(foundItemEntity -> {
					FoundItemResponse response = new FoundItemResponse();
					response.setPublicIdOfFoundItem(foundItemEntity.getPublicIdOfFoundItem());
					response.setFoundItemName(foundItemEntity.getFoundItemName());
					response.setDescription(foundItemEntity.getDescription());
					response.setImageURL(foundItemEntity.getImageURL());
					response.setPredictedLocation(foundItemEntity.getPredictedLocation());
					response.setCreatedAt(foundItemEntity.getCreatedAt());
					response.setContactTo(foundItemEntity.getContactTo());

					UserEntity reporter = userRepo.findByEmail(foundItemEntity.getContactTo());
					if (reporter != null && reporter.getPrivilegeClass() != null) {
						response.setReporterPrivilege(reporter.getPrivilegeClass().name());
					} else {
						response.setReporterPrivilege("NORMAL");
					}

					return response;
				});

		return PagedResponseDTO.from(mapped);
	}

	@Override
	@Transactional
	public Result closeFoundItem(String email, String publicId) {
		if (email == null || publicId == null) {
			throw new BadRequestException("Invalid credentials");
		}

		UUID publicUUID = UUID.fromString(publicId);

		int rowsChanged = foundItemRepo.closeRequest(email, publicUUID);
		if (rowsChanged == 0) {
			return Result.FAILED;
		}
		return Result.SUCCESS;
	}
}
