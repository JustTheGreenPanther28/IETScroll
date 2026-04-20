package com.ietscroll.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AccountVerificationRequest(@NotNull int otp, @NotNull @NotBlank String email) {
}