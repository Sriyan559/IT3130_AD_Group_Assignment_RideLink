package com.ridelink.driver.controller;

import com.ridelink.driver.dto.request.RegisterVehicleRequest;
import com.ridelink.driver.dto.response.VehicleResponse;
import com.ridelink.driver.service.VehicleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/drivers/{driverId}/vehicles")
public class VehicleController {
    private final VehicleService service;

    public VehicleController(VehicleService service) {
        this.service = service;
    }

    @Operation(summary = "Register a vehicle for an existing driver",
            description = "Does not change driver availability. Authorization is pending integration.")
    @ApiResponse(responseCode = "201", description = "Vehicle registered")
    @ApiResponse(responseCode = "400", description = "Invalid request")
    @ApiResponse(responseCode = "404", description = "Driver not found")
    @ApiResponse(responseCode = "409", description = "Plate number already registered")
    @ApiResponse(responseCode = "503", description = "Database unavailable")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VehicleResponse register(@PathVariable("driverId") String driverId,
                                    @Valid @RequestBody RegisterVehicleRequest request) {
        return service.register(driverId, request);
    }
}
