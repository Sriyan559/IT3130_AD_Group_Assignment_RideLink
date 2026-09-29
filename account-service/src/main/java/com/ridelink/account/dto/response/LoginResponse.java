package com.ridelink.account.dto.response;

public record LoginResponse(String token, String tokenType, long expiresInMs, AccountResponse account) {}
