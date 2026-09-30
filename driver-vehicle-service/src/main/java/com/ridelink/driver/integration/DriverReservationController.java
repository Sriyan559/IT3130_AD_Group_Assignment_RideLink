package com.ridelink.driver.integration;
import com.ridelink.driver.dto.response.DriverResponse;
import com.ridelink.driver.service.DriverReservationService;
import java.util.UUID;
import org.springframework.web.bind.annotation.*;
@io.swagger.v3.oas.annotations.Hidden
@RestController
@RequestMapping("/internal/drivers")
public class DriverReservationController {
    @org.springframework.beans.factory.annotation.Autowired
    private com.ridelink.driver.service.EligibleDriversService eligible;
    @GetMapping("/eligible")
    public java.util.List<com.ridelink.driver.dto.response.EligibleDriverResponse> eligible(
            @jakarta.validation.Valid @ModelAttribute com.ridelink.driver.dto.request.EligibleDriversRequest request) {
        return eligible.find(request);
    }
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
