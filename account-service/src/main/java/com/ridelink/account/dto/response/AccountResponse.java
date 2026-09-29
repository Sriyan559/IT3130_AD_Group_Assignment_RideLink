package com.ridelink.account.dto.response;

import com.ridelink.account.domain.AccountStatus;
import com.ridelink.account.domain.Role;

import java.time.Instant;

public record AccountResponse(
        String id,
        String fullName,
        String email,
        Role role,
        String phone,
        AccountStatus status,
        Instant createdAt,
        Instant updatedAt
) {}
