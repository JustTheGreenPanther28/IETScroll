package com.ietscroll.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;

public record FoundItemRequest(@NotNull @NotBlank String foundItemname, @Null String predictedLocation,
		@NotNull @NotBlank String description) {

}
