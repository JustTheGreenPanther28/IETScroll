package com.ietscroll.service;

import java.util.UUID;

import com.ietscroll.dto.LostItemDTO;
import com.ietscroll.dto.PagedResponseDTO;
import com.ietscroll.response.Result;

public interface LostItemService { 
	
	PagedResponseDTO<LostItemDTO> getAllLostItems(int page, int size, String sortBy, String order);
	PagedResponseDTO<LostItemDTO> getMyLostItems(String email,int page, int size);
	
	Result uploadLostItem(LostItemDTO listItemDTO);
	Result closeLostItem(String email,UUID publicId);
	

}
