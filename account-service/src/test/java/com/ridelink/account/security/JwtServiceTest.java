package com.ridelink.account.security;

import com.ridelink.account.document.Account;
import com.ridelink.account.domain.Role;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {
    private static final String SECRET = "01234567890123456789012345678901";

    @Test void signedTokenContainsSafeClaims() {
        JwtService service = new JwtService(SECRET, 60_000);
        Account account = new Account(); account.setId("a1"); account.setEmail("a@example.com"); account.setRole(Role.DRIVER);
        var claims = service.parse(service.generate(account));
        assertEquals("a1", claims.getSubject());
        assertEquals("DRIVER", claims.get("role"));
        assertNull(claims.get("passwordHash"));
    }

    @Test void expiredTokenRejected() throws InterruptedException {
        JwtService service = new JwtService(SECRET, 1);
        Account account = new Account(); account.setId("a1"); account.setEmail("a@example.com"); account.setRole(Role.PASSENGER);
        String token = service.generate(account);
        Thread.sleep(5);
        assertThrows(ExpiredJwtException.class, () -> service.parse(token));
    }

    @Test void shortSecretRejected() {
        assertThrows(IllegalArgumentException.class, () -> new JwtService("short", 1000));
    }
}
