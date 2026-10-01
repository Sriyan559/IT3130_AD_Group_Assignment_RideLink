package com.ridelink.account.service;

import com.ridelink.account.document.Account;
import com.ridelink.account.domain.AccountStatus;
import com.ridelink.account.domain.Role;
import com.ridelink.account.dto.request.LoginRequest;
import com.ridelink.account.dto.request.RegistrationRequest;
import com.ridelink.account.dto.response.AccountResponse;
import com.ridelink.account.dto.response.LoginResponse;
import com.ridelink.account.exception.AccountUnavailableException;
import com.ridelink.account.exception.DuplicateEmailException;
import com.ridelink.account.exception.InvalidCredentialsException;
import com.ridelink.account.mapper.AccountMapper;
import com.ridelink.account.repository.AccountRepository;
import com.ridelink.account.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class AuthService {
    private final AccountRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AccountMapper mapper;

    public AuthService(AccountRepository repository, PasswordEncoder passwordEncoder, JwtService jwtService,
                       AccountMapper mapper) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.mapper = mapper;
    }

    public AccountResponse registerPassenger(RegistrationRequest request) { return register(request, Role.PASSENGER); }
    public AccountResponse registerDriver(RegistrationRequest request) { return register(request, Role.DRIVER); }
    public AccountResponse registerAdmin(RegistrationRequest request) { return register(request, Role.ADMIN); }

    public LoginResponse login(LoginRequest request) {
        Account account = repository.findByEmail(normalize(request.email()))
                .orElseThrow(InvalidCredentialsException::new);
        if (!passwordEncoder.matches(request.password(), account.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }
        if (account.getStatus() != AccountStatus.ACTIVE) throw new AccountUnavailableException(account.getStatus());
        return new LoginResponse(jwtService.generate(account), "Bearer", jwtService.getExpirationMs(), mapper.toResponse(account));
    }

    private AccountResponse register(RegistrationRequest request, Role role) {
        String email = normalize(request.email());
        if (repository.existsByEmail(email)) throw new DuplicateEmailException();
        Account account = new Account();
        account.setFullName(request.fullName().trim());
        account.setEmail(email);
        account.setPasswordHash(passwordEncoder.encode(request.password()));
        account.setPhone(request.phone().trim());
        account.setRole(role);
        account.setStatus(AccountStatus.ACTIVE);
        return mapper.toResponse(repository.save(account));
    }

    static String normalize(String email) { return email.trim().toLowerCase(Locale.ROOT); }
}
