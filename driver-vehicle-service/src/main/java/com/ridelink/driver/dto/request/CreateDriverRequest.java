package com.ridelink.driver.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateDriverRequest(
        @NotBlank @Size(max = 100) String accountId,
        @NotBlank @Size(max = 50) String licenseNumber,
        @NotBlank @Size(max = 100) String serviceArea) {
}
