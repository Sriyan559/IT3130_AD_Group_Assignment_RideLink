package com.ridelink.account.security;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("accountAuthorization")
public class AccountAuthorization {
    public boolean canAccess(String accountId, Authentication authentication) {
        return authentication != null && (accountId.equals(authentication.getName()) ||
                authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }
}
