package com.ridelink.account;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * RideLink Account Service Application.
 * <p>
 * Primary Owner: Fernando B S C (Student ID: IT24103775)
 * Architectural Role: Microservice responsible for user account registration (passengers and drivers),
 * authentication, JWT token issuance, role-based access management, and profile lifecycle.
 * </p>
 */
@SpringBootApplication
public class AccountServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AccountServiceApplication.class, args);
    }
}
