package com.ietscroll.request;

import org.hibernate.validator.constraints.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TeamJoiningRequest(@NotNull @NotBlank @UUID String teamId,
		@NotNull @NotBlank String message) {

}
