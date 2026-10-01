package com.ridelink.account.config;

import com.ridelink.account.document.Account;
import com.ridelink.account.domain.AccountStatus;
import com.ridelink.account.domain.Role;
import com.ridelink.account.repository.AccountRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminBootstrapTest {
    private final AccountRepository repository = mock(AccountRepository.class);
    private final PasswordEncoder encoder = mock(PasswordEncoder.class);

    @Test
    void createsActiveAdminWhenMissing() throws Exception {
        when(repository.findByEmail("admin@example.com")).thenReturn(Optional.empty());
        when(encoder.encode("password123")).thenReturn("hash");
        AdminBootstrap bootstrap = new AdminBootstrap(repository, encoder, " ADMIN@example.com ",
                "password123", "RideLink Admin", "+94700000000");

        bootstrap.run(null);

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        verify(repository).save(captor.capture());
        Account admin = captor.getValue();
        assertAll(
                () -> assertEquals("admin@example.com", admin.getEmail()),
                () -> assertEquals("hash", admin.getPasswordHash()),
                () -> assertEquals(Role.ADMIN, admin.getRole()),
                () -> assertEquals(AccountStatus.ACTIVE, admin.getStatus())
        );
    }

    @Test
    void doesNotOverwriteExistingAdmin() throws Exception {
        Account existing = new Account();
        existing.setRole(Role.ADMIN);
        when(repository.findByEmail("admin@example.com")).thenReturn(Optional.of(existing));

        new AdminBootstrap(repository, encoder, "admin@example.com", "password123",
                "RideLink Admin", "+94700000000").run(null);

        verify(repository, never()).save(any());
        verify(encoder, never()).encode(any());
    }

    @Test
    void rejectsExistingNonAdminInsteadOfEscalatingPrivileges() {
        Account existing = new Account();
        existing.setRole(Role.PASSENGER);
        when(repository.findByEmail("admin@example.com")).thenReturn(Optional.of(existing));

        AdminBootstrap bootstrap = new AdminBootstrap(repository, encoder, "admin@example.com",
                "password123", "RideLink Admin", "+94700000000");

        assertThrows(IllegalStateException.class, () -> bootstrap.run(null));
        verify(repository, never()).save(any());
    }
}
