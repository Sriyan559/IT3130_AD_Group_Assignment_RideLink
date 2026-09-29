package com.ridelink.driver.integration;
import com.ridelink.driver.dto.response.DriverResponse;
import com.ridelink.driver.service.DriverReservationService;
import java.util.UUID;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/internal/drivers")
public class DriverReservationController {
    private final DriverReservationService reservations;
    public DriverReservationController(DriverReservationService reservations) { this.reservations = reservations; }
    @GetMapping("/{driverId}")
    public DriverResponse get(@PathVariable String driverId) { return DriverResponse.from(reservations.get(driverId)); }
    @PutMapping("/{driverId}/reservations/{rideId}")
    public DriverResponse reserve(@PathVariable String driverId, @PathVariable UUID rideId) {
        return DriverResponse.from(reservations.reserve(driverId, rideId));
    }
    @DeleteMapping("/{driverId}/reservations/{rideId}")
    public DriverResponse release(@PathVariable String driverId, @PathVariable UUID rideId) {
        return DriverResponse.from(reservations.release(driverId, rideId));
    }
}
