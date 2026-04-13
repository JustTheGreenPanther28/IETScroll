package com.ietscroll.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TeamCreationRequest(@NotBlank @NotNull @Size(min=10,max=300) String purpose ,@NotNull @NotBlank @Min(1) @Max(10) int teamSize) {

}
