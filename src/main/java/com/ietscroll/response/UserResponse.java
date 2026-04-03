package com.ietscroll.response;

import java.time.Year;
import java.util.UUID;

import com.ietscroll.general.enums.Branch;
import com.ietscroll.general.enums.Course;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UserResponse(@NotNull UUID publicUserId, @NotNull @NotBlank String username, @Email @NotBlank  String email, String fullName,
		Year yearOfPassout, Course course, Branch branch) {

}
