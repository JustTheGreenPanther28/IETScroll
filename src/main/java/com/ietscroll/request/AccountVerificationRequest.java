package com.ietscroll.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AccountVerificationRequest(@NotNull int otp, @Email @NotNull @NotBlank String email) {
}