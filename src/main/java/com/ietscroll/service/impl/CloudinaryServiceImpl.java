package com.ietscroll.service.impl;

import java.io.IOException;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.ietscroll.service.CloudinaryService;

@Service
public class CloudinaryServiceImpl implements CloudinaryService {

	private final Cloudinary cloudinary;

	public CloudinaryServiceImpl(Cloudinary cloudinary) {
		this.cloudinary = cloudinary;
	}

	//The second of upload and destroy is map
	//With help of you can alter image storage in CLOUDNIARY, LINK :
	//Map.of("folder", "lost-items") <- this will create folder and store image in it
	//Map.of("public_id", "item_123") <- set file name (unique)
	//Map.of("public_id", "item_123", "overwrite", true) <- replace existing image
	//Map.of("resource_type", "image") <- the MULTIPARTFILE can be image,video or raw etc
	@Override
	public Map upload(MultipartFile image) throws IOException {
		return cloudinary.uploader().upload(image.getBytes(), 
				Map.of(
						"folder","IETSCROLL_Lost_Items"
						));
	}

	@Override
	public void deleteImage(String publicId) throws IOException {
		cloudinary.uploader()
		.destroy(publicId, Map.of(
				"folder","IETSCROLL_Lost_Items",
				"invalidate", true
				));
	}
}
