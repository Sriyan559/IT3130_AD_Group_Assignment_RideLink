package com.ridelink.payment.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ridelink.payment.security.PassengerAccessPolicy;
import com.ridelink.support.AccessFailure;
import com.ridelink.support.AccountAccessFilter;
import com.ridelink.support.Identity;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Authentication for every request to this service.
 * <ul>
 *   <li>{@code /api/**}: the Bearer JWT is checked with Account Service (signature, role and
 *       ACTIVE status). Only PASSENGER and ADMIN accounts may use fare/payment APIs.</li>
 *   <li>{@code /internal/**}: service-to-service calls must send the shared X-Service-Token.</li>
 * </ul>
 * Swagger UI and API docs are not filtered.
 */
@Component
public class PaymentAccessFilter extends AccountAccessFilter {

    public PaymentAccessFilter(ObjectMapper json,
                               @Value("${ridelink.account-url:http://localhost:8081}") String accountUrl,
                               @Value("${ridelink.service-token:}") String serviceToken) {
        super(accountUrl, serviceToken, json);
    }

    @Override
    protected void authorize(HttpServletRequest request, Identity identity) {
        requirePassengerOrAdmin(identity);
    }

    /** Drivers (and any other role) cannot use fare or payment APIs. */
    public static void requirePassengerOrAdmin(Identity identity) {
        String role = identity.role();
        if (!PassengerAccessPolicy.PASSENGER.equals(role) && !PassengerAccessPolicy.ADMIN.equals(role)) {
            throw new AccessFailure(403, "Passenger account required");
        }
    }
}
