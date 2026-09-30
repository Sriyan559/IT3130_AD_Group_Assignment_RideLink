package com.ridelink.payment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * RideLink Fare & Payment Service Application.
 * <p>
 * Primary Owner: Sanjeewa H.D.U.S (Student ID: IT24101590)
 * Architectural Role: Microservice responsible for upfront fare estimation, deterministic final fare calculation,
 * simulated payment transaction processing, payment status tracking, and receipt generation.
 * </p>
 */
@SpringBootApplication
public class FarePaymentServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(FarePaymentServiceApplication.class, args);
    }
}
