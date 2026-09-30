package com.ridelink.driver.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record EligibleDriversRequest(
        @NotNull @DecimalMin("-90") @DecimalMax("90") BigDecimal lat,
        @NotNull @DecimalMin("-180") @DecimalMax("180") BigDecimal lng,
        @NotNull @DecimalMin(value = "0", inclusive = false) @DecimalMax("50") BigDecimal radius) {
}
