package com.ridelink.ride.controller;

import com.ridelink.ride.dto.request.CreateRideRequest;
import com.ridelink.ride.dto.request.UpdateRideStatusRequest;
import com.ridelink.ride.dto.response.RideResponse;
import com.ridelink.ride.service.RideService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/rides")
public class RideController {

     @GetMapping("/")
    public String home() {
        return "RideLink Ride Management Service is running";
    }

    private final RideService service;

    public RideController(RideService service) { this.service = service; }

    @PostMapping
    public ResponseEntity<RideResponse> create(@Valid @RequestBody CreateRideRequest request) {
        RideResponse response = service.create(request);
        return ResponseEntity.created(URI.create("/api/v1/rides/" + response.id())).body(response);
    }

    @GetMapping("/{rideId}")
    public RideResponse get(@PathVariable UUID rideId) { return service.get(rideId); }

    @PatchMapping("/{rideId}/status")
    public RideResponse updateStatus(@PathVariable UUID rideId,
                                     @Valid @RequestBody UpdateRideStatusRequest request) {
        return service.updateStatus(rideId, request);
    }

    @GetMapping("/passenger/{passengerId}")
    public List<RideResponse> history(@PathVariable Long passengerId) { return service.history(passengerId); }
    
}
    