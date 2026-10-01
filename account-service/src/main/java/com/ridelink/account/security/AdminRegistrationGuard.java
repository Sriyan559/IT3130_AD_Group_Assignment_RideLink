package com.ridelink.account.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Component
public class AdminRegistrationGuard {
    private final boolean enabled;
    private final byte[] configuredKey;

    public AdminRegistrationGuard(
            @Value("${account.admin.registration.enabled:false}") boolean enabled,
            @Value("${account.admin.registration.key:}") String configuredKey) {
        this.enabled = enabled;
        this.configuredKey = configuredKey.getBytes(StandardCharsets.UTF_8);
    }

    public void check(String suppliedKey) {
        byte[] supplied = suppliedKey == null ? new byte[0] : suppliedKey.getBytes(StandardCharsets.UTF_8);
        if (!enabled || configuredKey.length < 16 || !MessageDigest.isEqual(configuredKey, supplied)) {
            throw new AccessDeniedException("Admin registration is disabled or the registration key is invalid");
        }
    }
}
