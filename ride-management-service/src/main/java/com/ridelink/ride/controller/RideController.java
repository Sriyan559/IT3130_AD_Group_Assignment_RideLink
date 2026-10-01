package com.ridelink.ride.controller;

import com.ridelink.ride.domain.RideStatus;
import com.ridelink.ride.dto.request.CreateRideRequest;
import com.ridelink.ride.dto.request.UpdateRideStatusRequest;
import com.ridelink.ride.dto.response.RideResponse;
import com.ridelink.ride.service.RideService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import static java.util.Objects.requireNonNull;

/**
 * REST Controller exposing Ride Management endpoints.
 * Owner: Herath H M S R (IT24103280)
 */
@RestController
@RequestMapping("/api/v1/rides")
@Tag(name = "Ride Management API", description = "Endpoints for booking rides, tracking lifecycle status, assigning drivers, and viewing history")
public class RideController {
    @org.springframework.beans.factory.annotation.Value("${ridelink.security.enabled:true}")
    private boolean securityEnabled;

    private final RideService service;

    public RideController(RideService service) {
        this.service = service;
    }

    @PostMapping("/{rideId}/assign")
    @Operation(summary="Find and reserve the nearest eligible driver")
    public RideResponse match(@PathVariable UUID rideId,
            @RequestParam(defaultValue="5") java.math.BigDecimal radius) {
        return service.matchDriver(rideId,radius);
    }

    @PostMapping
    @Operation(summary = "Create a new ride request", description = "Initiates a ride booking. Sets status to REQUESTED (or ASSIGNED if driverId is provided).")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Ride successfully created",
                    content = @Content(schema = @Schema(implementation = RideResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error in request payload",
                    content = @Content(schema = @Schema(example = "{\"timestamp\":\"2026-09-29T10:00:00Z\",\"status\":400,\"error\":\"pickupAddress: pickupAddress is required\"}")))
    })
    public ResponseEntity<RideResponse> create(@Valid @RequestBody CreateRideRequest request) {
        if (securityEnabled) com.ridelink.support.Identity.current().requireOwner(request.passengerId(), "PASSENGER");
        RideResponse response = service.create(request);
        return ResponseEntity.created(requireNonNull(URI.create("/api/v1/rides/" + response.id()))).body(response);
    }

    @GetMapping
    @Operation(summary = "Get all rides", description = "Retrieves all rides, optionally filtered by status (e.g. REQUESTED, ASSIGNED, ACCEPTED, IN_PROGRESS, COMPLETED, CANCELLED).")
    @ApiResponse(responseCode = "200", description = "List of rides",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = RideResponse.class))))
    public List<RideResponse> getAll(
            @Parameter(description = "Optional status filter (e.g. REQUESTED, IN_PROGRESS)")
            @RequestParam(required = false) RideStatus status) {
        return service.getAll(status);
    }

    @GetMapping("/{rideId}")
    @Operation(summary = "Get ride details by ID", description = "Retrieves the complete details of a specific ride by its UUID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ride found",
                    content = @Content(schema = @Schema(implementation = RideResponse.class))),
            @ApiResponse(responseCode = "404", description = "Ride not found",
                    content = @Content(schema = @Schema(example = "{\"timestamp\":\"2026-09-29T10:00:00Z\",\"status\":404,\"error\":\"Ride not found: 550e8400-e29b-41d4-a716-446655440000\"}")))
    })
    public RideResponse get(
            @Parameter(description = "UUID of the ride to retrieve", required = true)
            @PathVariable UUID rideId) {
        return service.get(rideId);
    }

    @PatchMapping("/{rideId}/status")
    @Operation(summary = "Transition ride status (State Machine)", description = "Transitions status following strict state rules: REQUESTED -> ASSIGNED -> ACCEPTED -> IN_PROGRESS -> COMPLETED / CANCELLED.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status updated successfully",
                    content = @Content(schema = @Schema(implementation = RideResponse.class))),
            @ApiResponse(responseCode = "409", description = "Invalid state transition",
                    content = @Content(schema = @Schema(example = "{\"timestamp\":\"2026-09-29T10:00:00Z\",\"status\":409,\"error\":\"Invalid ride status transition from REQUESTED to COMPLETED\"}"))),
            @ApiResponse(responseCode = "404", description = "Ride not found")
    })
    public RideResponse updateStatus(
            @Parameter(description = "UUID of the ride", required = true)
            @PathVariable UUID rideId,
            @Valid @RequestBody UpdateRideStatusRequest request) {
        return service.updateStatus(rideId, request);
    }

    @PatchMapping("/{rideId}/assign-driver/{driverId}")
    @Operation(summary = "Assign a driver to a ride", description = "Assigns the driver and moves status from REQUESTED to ASSIGNED.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Driver successfully assigned",
                    content = @Content(schema = @Schema(implementation = RideResponse.class))),
            @ApiResponse(responseCode = "404", description = "Ride not found")
    })
    public RideResponse assignDriver(
            @Parameter(description = "UUID of the ride", required = true)
            @PathVariable UUID rideId,
            @Parameter(description = "ID of the driver to assign", required = true)
            @PathVariable String driverId) {
        return service.assignDriver(rideId, driverId);
    }

    @GetMapping("/passenger/{passengerId}")
    @Operation(summary = "Get ride history for a passenger", description = "Returns all rides requested by the given passenger ID, sorted newest first.")
    @ApiResponse(responseCode = "200", description = "List of passenger rides",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = RideResponse.class))))
    public List<RideResponse> history(
            @Parameter(description = "ID of the passenger", required = true)
            @PathVariable String passengerId) {
        return service.history(passengerId);
    }

    @GetMapping("/driver/{driverId}")
    @Operation(summary = "Get rides assigned to a driver", description = "Returns all rides assigned to the given driver ID, sorted newest first.")
    @ApiResponse(responseCode = "200", description = "List of driver rides",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = RideResponse.class))))
    public List<RideResponse> driverRides(
            @Parameter(description = "ID of the driver", required = true)
            @PathVariable String driverId) {
        return service.driverRides(driverId);
    }

    @DeleteMapping("/{rideId}")
    @Operation(summary = "Cancel a ride", description = "Cancels a ride if it is in REQUESTED, ASSIGNED, or ACCEPTED state.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ride successfully cancelled"),
            @ApiResponse(responseCode = "409", description = "Cannot cancel ride once IN_PROGRESS or COMPLETED"),
            @ApiResponse(responseCode = "404", description = "Ride not found")
    })
    public ResponseEntity<Map<String, String>> cancel(
            @Parameter(description = "UUID of the ride to cancel", required = true)
            @PathVariable UUID rideId) {
        service.cancel(rideId);
        return ResponseEntity.ok(Map.of("message", "Ride cancelled successfully", "rideId", rideId.toString()));
    }
}
