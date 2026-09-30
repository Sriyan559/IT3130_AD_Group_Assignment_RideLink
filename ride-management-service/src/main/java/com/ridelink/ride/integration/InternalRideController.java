package com.ridelink.ride.integration;
import com.ridelink.ride.service.RideService;
import com.ridelink.ride.dto.response.RideResponse;
import java.util.UUID;
import org.springframework.web.bind.annotation.*;
@io.swagger.v3.oas.annotations.Hidden
@RestController
public class InternalRideController {
    private final RideService rides;
    public InternalRideController(RideService rides) {this.rides=rides;}
    @GetMapping("/internal/rides/{id}")
    public RideResponse get(@PathVariable UUID id) {return rides.get(id);}
}
