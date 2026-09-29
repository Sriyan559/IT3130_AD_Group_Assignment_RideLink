package com.ridelink.account.service;

import com.ridelink.account.document.Account;
import com.ridelink.account.domain.AccountStatus;
import com.ridelink.account.dto.request.UpdateAccountRequest;
import com.ridelink.account.dto.response.AccountResponse;
import com.ridelink.account.exception.AccountNotFoundException;
import com.ridelink.account.mapper.AccountMapper;
import com.ridelink.account.repository.AccountRepository;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class AccountService {
    private final AccountRepository repository;
    private final AccountMapper mapper;

    public AccountService(AccountRepository repository, AccountMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public AccountResponse get(String id) { return mapper.toResponse(find(id)); }

    public AccountResponse update(String id, UpdateAccountRequest request) {
        Account account = find(id);
        account.setFullName(request.fullName().trim());
        account.setPhone(request.phone().trim());
        return mapper.toResponse(repository.save(account));
    }

    public AccountResponse updateStatus(String id, AccountStatus status) {
        Account account = find(id);
        account.setStatus(status);
        return mapper.toResponse(repository.save(account));
    }

    private Account find(String id) {
        String accountId = Objects.requireNonNull(id, "Account ID is required");
        return repository.findById(accountId).orElseThrow(() -> new AccountNotFoundException(accountId));
    }
}
