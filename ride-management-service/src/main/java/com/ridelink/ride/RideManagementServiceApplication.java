package com.ridelink.ride;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * RideLink Ride Management Service Application.
 * <p>
 * Primary Owner: Herath H M S R (Student ID: IT24103280)
 * Architectural Role: Microservice responsible for orchestrating ride requests, managing the ride state machine
 * (REQUESTED, ASSIGNED, ACCEPTED, IN_PROGRESS, COMPLETED, CANCELLED), coordinating with driver availability,
 * and interfacing with fare/payment.
 * </p>
 */
@SpringBootApplication
public class RideManagementServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(RideManagementServiceApplication.class, args);
    }
}
