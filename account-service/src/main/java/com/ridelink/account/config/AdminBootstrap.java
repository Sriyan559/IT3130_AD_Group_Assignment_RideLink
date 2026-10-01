package com.ridelink.account.config;

import com.ridelink.account.document.Account;
import com.ridelink.account.domain.AccountStatus;
import com.ridelink.account.domain.Role;
import com.ridelink.account.repository.AccountRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Locale;

/** Creates the first administrator only when explicitly enabled through configuration. */
@Component
@ConditionalOnProperty(prefix = "account.admin.bootstrap", name = "enabled", havingValue = "true")
public class AdminBootstrap implements ApplicationRunner {
    private final AccountRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final String email;
    private final String password;
    private final String fullName;
    private final String phone;

    public AdminBootstrap(AccountRepository repository, PasswordEncoder passwordEncoder,
                          @Value("${account.admin.bootstrap.email:}") String email,
                          @Value("${account.admin.bootstrap.password:}") String password,
                          @Value("${account.admin.bootstrap.full-name:RideLink Administrator}") String fullName,
                          @Value("${account.admin.bootstrap.phone:+94000000000}") String phone) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.email = email;
        this.password = password;
        this.fullName = fullName;
        this.phone = phone;
    }

    @Override
    public void run(ApplicationArguments args) {
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        if (normalizedEmail.isBlank() || password.length() < 8 || password.length() > 72) {
            throw new IllegalStateException(
                    "Admin bootstrap requires a valid ACCOUNT_ADMIN_EMAIL and an 8-72 character ACCOUNT_ADMIN_PASSWORD");
        }

        repository.findByEmail(normalizedEmail).ifPresentOrElse(existing -> {
            if (existing.getRole() != Role.ADMIN) {
                throw new IllegalStateException("Admin bootstrap email already belongs to a non-admin account");
            }
        }, () -> {
            Account admin = new Account();
            admin.setFullName(fullName.trim());
            admin.setEmail(normalizedEmail);
            admin.setPasswordHash(passwordEncoder.encode(password));
            admin.setRole(Role.ADMIN);
            admin.setPhone(phone.trim());
            admin.setStatus(AccountStatus.ACTIVE);
            repository.save(admin);
        });
    }
}
