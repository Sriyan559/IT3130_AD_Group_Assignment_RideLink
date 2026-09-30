package com.ridelink.account.service;

import com.ridelink.account.document.Account;
import com.ridelink.account.domain.AccountStatus;
import com.ridelink.account.dto.request.UpdateAccountRequest;
import com.ridelink.account.exception.AccountNotFoundException;
import com.ridelink.account.mapper.AccountMapper;
import com.ridelink.account.repository.AccountRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SuppressWarnings("null")
class AccountServiceTest {
    private final AccountRepository repository = mock(AccountRepository.class);
    private final AccountService service = new AccountService(repository, new AccountMapper());

    @Test void getAccount_success() {
        when(repository.findById("a1")).thenReturn(Optional.of(account()));
        assertEquals("a1", service.get("a1").id());
    }

    @Test void getAccount_notFound() {
        assertThrows(AccountNotFoundException.class, () -> service.get("missing"));
    }

    @Test void updateProfile_success() {
        Account account = account();
        when(repository.findById("a1")).thenReturn(Optional.of(account));
        when(repository.save(account)).thenReturn(account);
        var response = service.update("a1", new UpdateAccountRequest(" Updated Name ", "+94770000000"));
        assertEquals("Updated Name", response.fullName());
        assertEquals(AccountStatus.ACTIVE, response.status());
    }

    @Test void updateStatus_adminFlow_success() {
        Account account = account();
        when(repository.findById("a1")).thenReturn(Optional.of(account));
        when(repository.save(account)).thenReturn(account);
        assertEquals(AccountStatus.SUSPENDED, service.updateStatus("a1", AccountStatus.SUSPENDED).status());
    }

    private Account account() {
        Account a = new Account(); a.setId("a1"); a.setFullName("Name"); a.setStatus(AccountStatus.ACTIVE); return a;
    }
}
