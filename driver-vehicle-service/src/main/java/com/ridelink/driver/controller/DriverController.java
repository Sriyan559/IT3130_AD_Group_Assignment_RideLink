package com.ridelink.driver.controller;

import com.ridelink.driver.dto.request.CreateDriverRequest;
import com.ridelink.driver.dto.request.UpdateAvailabilityRequest;
import com.ridelink.driver.service.DriverAvailabilityService;
import com.ridelink.driver.dto.response.DriverResponse;
import com.ridelink.driver.service.DriverService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/drivers")
public class DriverController {
    private final DriverService driverService;
    private final DriverAvailabilityService availabilityService;

    public DriverController(DriverService driverService, DriverAvailabilityService availabilityService) {
        this.driverService = driverService;
        this.availabilityService = availabilityService;
    }

    @Operation(summary = "Set driver availability to AVAILABLE or OFFLINE",
            description = "Repeated updates are idempotent. ON_TRIP drivers cannot toggle availability. Authorization is pending integration.")
    @ApiResponse(responseCode = "200", description = "Updated driver profile")
    @ApiResponse(responseCode = "400", description = "Invalid request")
    @ApiResponse(responseCode = "404", description = "Driver not found")
    @ApiResponse(responseCode = "409", description = "Driver is on a trip")
    @ApiResponse(responseCode = "503", description = "Database unavailable")
    @PutMapping("/{driverId}/availability")
    public DriverResponse updateAvailability(@PathVariable("driverId") String driverId,
                                              @Valid @RequestBody UpdateAvailabilityRequest request) {
        return availabilityService.update(driverId, request);
    }

    @Operation(summary = "Create a driver operational profile",
            description = "Creates an OFFLINE driver. Account verification and authorization are pending integration.")
    @ApiResponse(responseCode = "201", description = "Driver created")
    @ApiResponse(responseCode = "400", description = "Invalid request")
    @ApiResponse(responseCode = "409", description = "Account or licence already registered")
    @ApiResponse(responseCode = "503", description = "Database unavailable")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DriverResponse create(@Valid @RequestBody CreateDriverRequest request) {
        return driverService.create(request);
    }

    @Operation(summary = "Get a driver operational profile by driver ID",
            description = "Uses the ID returned by profile creation. Authorization is pending integration.")
    @ApiResponse(responseCode = "200", description = "Driver found")
    @ApiResponse(responseCode = "404", description = "Driver not found")
    @ApiResponse(responseCode = "503", description = "Database unavailable")
    @GetMapping("/{driverId}")
    public DriverResponse getById(@PathVariable("driverId") String driverId) {
        return driverService.getById(driverId);
    }
}
