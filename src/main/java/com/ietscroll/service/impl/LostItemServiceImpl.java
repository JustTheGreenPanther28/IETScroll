package com.ietscroll.service.impl;

import java.io.IOException;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.ietscroll.dto.LostItemDTO;
import com.ietscroll.dto.PagedResponseDTO;
import com.ietscroll.repository.LostItemRepository;
import com.ietscroll.repository.UserRepository;
import com.ietscroll.response.Result;
import com.ietscroll.service.LostItemService;
import com.ietscroll.service.SightEngineService;

@Service
public class LostItemServiceImpl implements LostItemService {

	private final LostItemRepository lostItemRepo;
	private final UserRepository userRepo;
	private final SightEngineService sightEngineService;
	
	
	public LostItemServiceImpl(LostItemRepository lostItemRepo, UserRepository userRepo,SightEngineService sightEngineService) {
		this.lostItemRepo = lostItemRepo;
		this.userRepo = userRepo;
		this.sightEngineService=sightEngineService;
	}
	
	@Override
	public Result uploadLostItem(LostItemDTO lostItemDTO,MultipartFile image) throws IOException {
		
		
		if(image==null || image.isEmpty() || !image.getResource().isReadable()) {
			throw new RuntimeException("Upload image only!");
		}
		
		System.out.println("hereeeeeeeeeeeeee");		
		sightEngineService.checkImage(image);
		System.out.println("what the hack");
		return null;
	}


	@Override
	public PagedResponseDTO<LostItemDTO> getAllLostItems(int page, int size, String sortBy, String order) {
		return null;
	}

	@Override
	public PagedResponseDTO<LostItemDTO> getMyLostItems(String email, int page, int size) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Result closeLostItem(String email, UUID publicId) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
