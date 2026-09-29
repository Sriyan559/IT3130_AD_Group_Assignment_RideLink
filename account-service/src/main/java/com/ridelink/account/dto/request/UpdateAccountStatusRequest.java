package com.ridelink.account.dto.request;

import com.ridelink.account.domain.AccountStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateAccountStatusRequest(@NotNull AccountStatus status) {}
