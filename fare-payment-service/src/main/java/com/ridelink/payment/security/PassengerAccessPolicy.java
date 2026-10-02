package com.ridelink.payment.security;

import com.ridelink.support.Identity;
import org.springframework.stereotype.Component;

/**
 * Resource-level authorisation: a passenger may only see or pay for their own rides,
 * fares, payments and receipts. ADMIN may access any of them.
 * <p>
 * {@link com.ridelink.payment.integration.PaymentAccessFilter} has already verified the JWT
 * with Account Service and stored the caller's {@link Identity} on the request.
 */
@Component
public class PassengerAccessPolicy {

    public static final String PASSENGER = "PASSENGER";
    public static final String ADMIN = "ADMIN";

    /** @throws com.ridelink.support.AccessFailure 403 when the caller is neither the owner nor an admin */
    public void requireOwnerOrAdmin(String passengerId) {
        Identity caller = Identity.current();
        if (ADMIN.equals(caller.role())) {
            return;
        }
        caller.requireOwner(passengerId, PASSENGER);
    }
}
