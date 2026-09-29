package com.ridelink.ride.integration;
import com.ridelink.ride.service.RideService;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.*;
@Configuration
@EnableScheduling
public class ReservationRecovery {
    private final RideService rides;
    public ReservationRecovery(RideService rides) { this.rides = rides; }
    @Scheduled(fixedDelayString="${ridelink.recovery-delay-ms:5000}", initialDelay=5000)
    public void retry() { rides.reconcile(); }
}
