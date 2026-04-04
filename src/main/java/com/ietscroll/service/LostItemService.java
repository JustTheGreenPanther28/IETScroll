package com.ietscroll.service;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

import com.ietscroll.dto.LostItemDTO;
import com.ietscroll.dto.PagedResponseDTO;
import com.ietscroll.response.LostItemResponse;
import com.ietscroll.response.Result;

public interface LostItemService { 
	
	PagedResponseDTO<LostItemResponse> getAllLostItems(int page, int size);
	List<LostItemDTO> getMyLostItems(String email);
	
	Result uploadLostItem(String email,LostItemDTO listItemDTO,MultipartFile image) throws IOException;
	Result closeLostItem(String email,String publicId);
	

}
