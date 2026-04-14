package com.ietscroll.request;

import java.time.Year;

import com.ietscroll.general.enums.Branch;
import com.ietscroll.general.enums.Course;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UserRegisterRequest(@NotBlank String username, String fullName, @Email @NotNull @NotBlank String email,
		Year yearOfPassout, @NotNull Course course, @NotNull Branch branch,
		@NotNull @NotBlank String password) {

}
