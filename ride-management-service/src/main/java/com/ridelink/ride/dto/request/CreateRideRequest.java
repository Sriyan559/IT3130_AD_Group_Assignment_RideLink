package com.ridelink.ride.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CreateRideRequest(
        @NotNull Long passengerId,
        @NotNull @DecimalMin("-90.0") @DecimalMax("90.0") BigDecimal pickupLatitude,
        @NotNull @DecimalMin("-180.0") @DecimalMax("180.0") BigDecimal pickupLongitude,
        @NotBlank String pickupAddress,
        @NotNull @DecimalMin("-90.0") @DecimalMax("90.0") BigDecimal destinationLatitude,
        @NotNull @DecimalMin("-180.0") @DecimalMax("180.0") BigDecimal destinationLongitude,
        @NotBlank String destinationAddress,
        Long driverId
) { }