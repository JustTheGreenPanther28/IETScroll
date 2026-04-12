package com.ietscroll.service;

import java.io.IOException;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.ietscroll.dto.FoundItemDTO;
import com.ietscroll.dto.PagedResponseDTO;
import com.ietscroll.response.FoundItemResponse;
import com.ietscroll.response.Result;

public interface FoundItemService {
	PagedResponseDTO<FoundItemResponse> getAllFoundItems(int page, int size);
	List<FoundItemDTO> getMyFoundItems(String email);
	
	Result uploadFoundItem(String email,FoundItemDTO foundItemDTO,MultipartFile image) throws IOException;
	Result closeFoundItemRequest(String email,String publicId);
}
