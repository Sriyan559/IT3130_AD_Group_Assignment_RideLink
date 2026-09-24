package com.ridelink.driver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * RideLink Driver & Vehicle Service Application.
 * <p>
 * Primary Owner: Rathnakoon D A (Student ID: IT24300246)
 * Architectural Role: Microservice responsible for driver operational profiles, vehicle registration,
 * real-time availability updates, simulated current GPS locations, and querying eligible available drivers.
 * </p>
 */
@SpringBootApplication
public class DriverVehicleServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DriverVehicleServiceApplication.class, args);
    }
}
