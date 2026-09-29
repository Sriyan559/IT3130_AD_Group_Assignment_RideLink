package com.ridelink.driver.controller;

import com.ridelink.driver.dto.request.EligibleDriversRequest;
import com.ridelink.driver.dto.response.EligibleDriverResponse;
import com.ridelink.driver.service.EligibleDriversService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/drivers")
public class EligibleDriversController {
    private final EligibleDriversService service;

    public EligibleDriversController(EligibleDriversService service) {
        this.service = service;
    }

    @Operation(summary = "Find nearby AVAILABLE drivers with registered vehicles",
            description = "Required lat/lng in degrees and radius in kilometres (greater than 0, maximum 50). "
                    + "Returns nearest first, or an empty list. Location freshness defaults to 300 seconds. "
                    + "Read-only discovery; does not reserve drivers. Authorization is pending integration.")
    @ApiResponse(responseCode = "200", description = "Eligible drivers, possibly empty")
    @ApiResponse(responseCode = "400", description = "Invalid or missing query parameters")
    @ApiResponse(responseCode = "503", description = "Database unavailable")
    @GetMapping("/eligible")
    public List<EligibleDriverResponse> find(@Valid @ModelAttribute @ParameterObject EligibleDriversRequest request) {
        return service.find(request);
    }
}
