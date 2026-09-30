package com.ridelink.account.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateAccountRequest(
        @NotBlank @Size(max = 100) String fullName,
        @NotBlank @Pattern(regexp = "^\\+?[0-9][0-9 -]{6,19}$", message = "must be a valid phone number") String phone
) {}
