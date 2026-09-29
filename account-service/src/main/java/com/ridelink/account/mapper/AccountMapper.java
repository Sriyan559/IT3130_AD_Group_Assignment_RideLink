package com.ridelink.account.mapper;

import com.ridelink.account.document.Account;
import com.ridelink.account.dto.response.AccountResponse;
import org.springframework.stereotype.Component;

@Component
public class AccountMapper {
    public AccountResponse toResponse(Account account) {
        return new AccountResponse(account.getId(), account.getFullName(), account.getEmail(), account.getRole(),
                account.getPhone(), account.getStatus(), account.getCreatedAt(), account.getUpdatedAt());
    }
}
