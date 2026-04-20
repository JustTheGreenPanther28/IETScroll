package com.ietscroll.request;

import java.util.List;

import com.ietscroll.general.enums.Privacy;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Size;

public record TeamCreationRequest(@NotBlank @NotNull @Size(min=10,max=300) String purpose ,@NotNull @NotBlank @Min(3) @Max(20) int teamSize,@Null List<Integer> skillIds,@NotNull Privacy privacy) {

}
