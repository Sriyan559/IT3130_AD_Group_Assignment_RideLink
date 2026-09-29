package com.ridelink.account.exception;

import com.ridelink.account.domain.AccountStatus;

public class AccountUnavailableException extends RuntimeException {
    private final AccountStatus status;
    public AccountUnavailableException(AccountStatus status) {
        super("Account is " + status.name().toLowerCase());
        this.status = status;
    }
    public AccountStatus getStatus() { return status; }
}
