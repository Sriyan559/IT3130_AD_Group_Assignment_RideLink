package com.ridelink.driver.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterVehicleRequest(
        @NotBlank @Size(max = 20)
        @Pattern(regexp = " *[A-Za-z0-9]+(?:[ -][A-Za-z0-9]+)* *",
                message = "must contain letters or digits separated by single spaces or hyphens")
        String plateNumber,
        @NotBlank @Size(max = 50) String make,
        @NotBlank @Size(max = 50) String model,
        @NotBlank @Size(max = 50) String vehicleClass,
        @NotNull @Min(1) Integer capacity) {
}
