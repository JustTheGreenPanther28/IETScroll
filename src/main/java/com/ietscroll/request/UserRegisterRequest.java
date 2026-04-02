package com.ietscroll.request;

import java.time.Year;

import com.ietscroll.entity.Branch;
import com.ietscroll.entity.Course;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UserRegisterRequest(@NotBlank String userName, String fullName, @Email @NotNull @NotBlank String email,
		Year yearOfPassout, @NotNull @NotBlank Course course, @NotNull @NotBlank Branch branch,
		@NotNull @NotBlank String password) {

}
