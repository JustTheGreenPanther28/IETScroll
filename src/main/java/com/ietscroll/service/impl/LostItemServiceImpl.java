package com.ietscroll.service.impl;

import java.util.UUID;

import com.ietscroll.dto.LostItemDTO;
import com.ietscroll.dto.PagedResponseDTO;
import com.ietscroll.repository.LostItemRepository;
import com.ietscroll.repository.UserRepository;
import com.ietscroll.response.Result;
import com.ietscroll.service.LostItemService;

public class LostItemServiceImpl implements LostItemService {

	private LostItemRepository lostItemRepo;
	private UserRepository userRepo;

	public LostItemServiceImpl(LostItemRepository lostItemRepo, UserRepository userRepo) {
		this.lostItemRepo = lostItemRepo;
		this.userRepo = userRepo;
	}
	
	@Override
	public Result uploadLostItem(LostItemDTO listItemDTO) {
		
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
