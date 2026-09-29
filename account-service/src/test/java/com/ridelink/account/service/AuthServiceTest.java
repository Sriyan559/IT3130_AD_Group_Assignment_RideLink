package com.ridelink.account.service;

import com.ridelink.account.document.Account;
import com.ridelink.account.domain.AccountStatus;
import com.ridelink.account.domain.Role;
import com.ridelink.account.dto.request.LoginRequest;
import com.ridelink.account.dto.request.RegistrationRequest;
import com.ridelink.account.exception.AccountUnavailableException;
import com.ridelink.account.exception.DuplicateEmailException;
import com.ridelink.account.exception.InvalidCredentialsException;
import com.ridelink.account.mapper.AccountMapper;
import com.ridelink.account.repository.AccountRepository;
import com.ridelink.account.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class AuthServiceTest {
    @Mock AccountRepository repository;
    @Mock PasswordEncoder encoder;
    @Mock JwtService jwtService;
    private AuthService service;

    @BeforeEach void setUp() { service = new AuthService(repository, encoder, jwtService, new AccountMapper()); }

    @Test void registerPassenger_success() {
        when(encoder.encode("password1")).thenReturn("hash");
        when(repository.save(any())).thenAnswer(inv -> { Account a = inv.getArgument(0); a.setId("p1"); return a; });
        var result = service.registerPassenger(request(" Person@Example.COM "));
        assertEquals(Role.PASSENGER, result.role());
        assertEquals("person@example.com", result.email());
        verify(repository).save(argThat(a -> a.getPasswordHash().equals("hash") && a.getStatus() == AccountStatus.ACTIVE));
    }

    @Test void registerDriver_success() {
        when(encoder.encode(any())).thenReturn("hash");
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        assertEquals(Role.DRIVER, service.registerDriver(request("driver@example.com")).role());
    }

    @Test void register_duplicateEmail_throwsConflict() {
        when(repository.existsByEmail("person@example.com")).thenReturn(true);
        assertThrows(DuplicateEmailException.class, () -> service.registerPassenger(request("PERSON@example.com")));
        verify(repository, never()).save(any());
    }

    @Test void login_validCredentials_returnsJwt() {
        Account account = account(AccountStatus.ACTIVE);
        when(repository.findByEmail("person@example.com")).thenReturn(Optional.of(account));
        when(encoder.matches("password1", "hash")).thenReturn(true);
        when(jwtService.generate(account)).thenReturn("jwt");
        when(jwtService.getExpirationMs()).thenReturn(3600000L);
        var result = service.login(new LoginRequest("PERSON@example.com", "password1"));
        assertEquals("jwt", result.token());
        assertEquals("Bearer", result.tokenType());
    }

    @Test void login_invalidPassword_throwsUnauthorized() {
        Account account = account(AccountStatus.ACTIVE);
        when(repository.findByEmail("person@example.com")).thenReturn(Optional.of(account));
        when(encoder.matches(any(), any())).thenReturn(false);
        assertThrows(InvalidCredentialsException.class,
                () -> service.login(new LoginRequest("person@example.com", "wrong")));
    }

    @Test void login_unknownEmail_throwsUnauthorized() {
        assertThrows(InvalidCredentialsException.class,
                () -> service.login(new LoginRequest("missing@example.com", "password1")));
    }

    @Test void login_suspendedAccount_rejected() { assertUnavailable(AccountStatus.SUSPENDED); }
    @Test void login_disabledAccount_rejected() { assertUnavailable(AccountStatus.DISABLED); }

    private void assertUnavailable(AccountStatus status) {
        Account account = account(status);
        when(repository.findByEmail("person@example.com")).thenReturn(Optional.of(account));
        when(encoder.matches(any(), any())).thenReturn(true);
        assertThrows(AccountUnavailableException.class,
                () -> service.login(new LoginRequest("person@example.com", "password1")));
        verify(jwtService, never()).generate(any());
    }

    private RegistrationRequest request(String email) {
        return new RegistrationRequest("Person", email, "password1", "+94771234567");
    }
    private Account account(AccountStatus status) {
        Account a = new Account(); a.setId("p1"); a.setFullName("Person"); a.setEmail("person@example.com");
        a.setPasswordHash("hash"); a.setPhone("+94771234567"); a.setRole(Role.PASSENGER); a.setStatus(status); return a;
    }
}
