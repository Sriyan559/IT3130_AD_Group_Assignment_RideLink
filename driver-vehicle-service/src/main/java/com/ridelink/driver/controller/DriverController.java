package com.ridelink.driver.controller;

import com.ridelink.driver.dto.request.CreateDriverRequest;
import com.ridelink.driver.dto.response.DriverResponse;
import com.ridelink.driver.service.DriverService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/drivers")
public class DriverController {
    private final DriverService driverService;

    public DriverController(DriverService driverService) {
        this.driverService = driverService;
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
}
