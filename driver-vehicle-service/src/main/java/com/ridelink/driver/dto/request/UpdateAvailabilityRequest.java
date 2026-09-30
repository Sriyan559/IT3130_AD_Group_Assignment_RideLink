package com.ridelink.driver.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record UpdateAvailabilityRequest(
        @NotNull
        @Pattern(regexp = "AVAILABLE|OFFLINE", message = "must be AVAILABLE or OFFLINE")
        String availabilityStatus) {
}
