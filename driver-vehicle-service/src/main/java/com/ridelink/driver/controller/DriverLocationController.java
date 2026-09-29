package com.ridelink.driver.controller;

import com.ridelink.driver.dto.request.UpdateLocationRequest;
import com.ridelink.driver.dto.response.DriverResponse;
import com.ridelink.driver.service.DriverLocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/drivers")
public class DriverLocationController {
    private final DriverLocationService service;

    public DriverLocationController(DriverLocationService service) {
        this.service = service;
    }

    @Operation(summary = "Update simulated driver coordinates",
            description = "Accepts coordinates in any availability state. Sets a server timestamp and preserves profile and status. Requires the owning DRIVER account token.")
    @ApiResponse(responseCode = "200", description = "Updated driver profile with location")
    @ApiResponse(responseCode = "400", description = "Invalid coordinates or request")
    @ApiResponse(responseCode = "404", description = "Driver not found")
    @ApiResponse(responseCode = "503", description = "Database unavailable")
    @PutMapping("/{driverId}/location")
    public DriverResponse update(@PathVariable("driverId") String driverId,
                                 @Valid @RequestBody UpdateLocationRequest request) {
        return service.update(driverId, request);
    }
}
