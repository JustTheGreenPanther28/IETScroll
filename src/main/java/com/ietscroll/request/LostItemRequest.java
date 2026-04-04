package com.ietscroll.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;

public record LostItemRequest(@NotNull @NotBlank String lostItemname, @Null String predictedLocation,
		@NotNull @NotBlank String description, int prize) {

}