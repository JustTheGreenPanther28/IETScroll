package com.ietscroll.response;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LostItemReponse(@NotNull @NotBlank UUID publicId, @NotNull @NotBlank String lostItemname,
		@NotNull @NotBlank String imageURLOfItem, String predictedLocation, @NotBlank @NotNull String description,
		int prize) {

}
