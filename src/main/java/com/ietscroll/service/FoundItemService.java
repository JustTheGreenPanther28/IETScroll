package com.ietscroll.service;

import java.io.IOException;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.ietscroll.dto.LostItemDTO;
import com.ietscroll.dto.PagedResponseDTO;
import com.ietscroll.response.LostItemResponse;
import com.ietscroll.response.Result;

public interface FoundItemService {
	PagedResponseDTO<LostItemResponse> getAllFoundItems(int page, int size);
	List<LostItemDTO> getMyFoundItems(String email);
	
	Result uploadFoundItem(String email,LostItemDTO listItemDTO,MultipartFile image) throws IOException;
	Result closeFoundItemRequest(String email,String publicId);
}
