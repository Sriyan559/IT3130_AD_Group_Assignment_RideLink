package com.ridelink.ride.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;

@Schema(description = "Request payload for creating a new ride request")
public record CreateRideRequest(
        @Schema(description = "ID of the passenger requesting the ride", example = "6abbfa6d095b5451ceb2f209")
        @NotBlank(message = "passengerId is required")
        String passengerId,

        @Schema(description = "Pickup location latitude (-90.0 to 90.0)", example = "6.9271")
        @NotNull(message = "pickupLatitude is required")
        @DecimalMin(value = "-90.0", message = "pickupLatitude must be >= -90.0")
        @DecimalMax(value = "90.0", message = "pickupLatitude must be <= 90.0")
        BigDecimal pickupLatitude,

        @Schema(description = "Pickup location longitude (-180.0 to 180.0)", example = "79.8612")
        @NotNull(message = "pickupLongitude is required")
        @DecimalMin(value = "-180.0", message = "pickupLongitude must be >= -180.0")
        @DecimalMax(value = "180.0", message = "pickupLongitude must be <= 180.0")
        BigDecimal pickupLongitude,

        @Schema(description = "Human-readable pickup address / landmark", example = "Colombo Fort Railway Station, Colombo")
        @NotBlank(message = "pickupAddress is required")
        String pickupAddress,

        @Schema(description = "Destination latitude (-90.0 to 90.0)", example = "6.9147")
        @NotNull(message = "destinationLatitude is required")
        @DecimalMin(value = "-90.0", message = "destinationLatitude must be >= -90.0")
        @DecimalMax(value = "90.0", message = "destinationLatitude must be <= 90.0")
        BigDecimal destinationLatitude,

        @Schema(description = "Destination longitude (-180.0 to 180.0)", example = "79.8732")
        @NotNull(message = "destinationLongitude is required")
        @DecimalMin(value = "-180.0", message = "destinationLongitude must be >= -180.0")
        @DecimalMax(value = "180.0", message = "destinationLongitude must be <= 180.0")
        BigDecimal destinationLongitude,

        @Schema(description = "Human-readable destination address / landmark", example = "SLIIT Metropolitan Campus, Colombo 03")
        @NotBlank(message = "destinationAddress is required")
        String destinationAddress,

        @Schema(description = "Optional assigned driver ID at ride booking time", example = "6abbfa6d095b5451ceb2f20a", nullable = true)
        @Pattern(regexp = "\\S+", message = "driverId must be nonblank and contain no whitespace")
        String driverId
) { }