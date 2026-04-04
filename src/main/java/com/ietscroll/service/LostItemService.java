package com.ietscroll.service;

import java.io.IOException;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

import com.ietscroll.dto.LostItemDTO;
import com.ietscroll.dto.PagedResponseDTO;
import com.ietscroll.response.Result;

public interface LostItemService { 
	
	PagedResponseDTO<LostItemDTO> getAllLostItems(int page, int size, String sortBy, String order);
	PagedResponseDTO<LostItemDTO> getMyLostItems(String email,int page, int size);
	
	Result uploadLostItem(LostItemDTO listItemDTO,MultipartFile image) throws IOException;
	Result closeLostItem(String email,UUID publicId);
	

}
