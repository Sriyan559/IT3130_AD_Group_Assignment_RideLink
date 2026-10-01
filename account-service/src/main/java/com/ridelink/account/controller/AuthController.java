package com.ridelink.account.controller;

import com.ridelink.account.dto.request.LoginRequest;
import com.ridelink.account.dto.request.RegistrationRequest;
import com.ridelink.account.dto.response.AccountResponse;
import com.ridelink.account.dto.response.LoginResponse;
import com.ridelink.account.service.AuthService;
import com.ridelink.account.security.AdminRegistrationGuard;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@SecurityRequirements
public class AuthController {
    private final AuthService service;
    private final AdminRegistrationGuard adminRegistrationGuard;
    public AuthController(AuthService service, AdminRegistrationGuard adminRegistrationGuard) {
        this.service = service;
        this.adminRegistrationGuard = adminRegistrationGuard;
    }

    @PostMapping("/register/passenger")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register passenger", description = "Creates an ACTIVE PASSENGER account; role and status are server controlled.")
    @ApiResponses({@ApiResponse(responseCode = "201", description = "Passenger created"),
            @ApiResponse(responseCode = "400", description = "Validation failure"),
            @ApiResponse(responseCode = "409", description = "Email already exists")})
    public AccountResponse registerPassenger(@Valid @RequestBody RegistrationRequest request) {
        return service.registerPassenger(request);
    }

    @PostMapping("/register/driver")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register driver identity", description = "Creates only an ACTIVE DRIVER account, not vehicle or availability data.")
    @ApiResponses({@ApiResponse(responseCode = "201", description = "Driver account created"),
            @ApiResponse(responseCode = "400", description = "Validation failure"),
            @ApiResponse(responseCode = "409", description = "Email already exists")})
    public AccountResponse registerDriver(@Valid @RequestBody RegistrationRequest request) {
        return service.registerDriver(request);
    }

    @PostMapping("/register/admin")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register administrator",
            description = "Creates an ACTIVE ADMIN only when privileged registration is explicitly enabled and the registration key is valid.")
    @ApiResponses({@ApiResponse(responseCode = "201", description = "Administrator created"),
            @ApiResponse(responseCode = "400", description = "Validation failure"),
            @ApiResponse(responseCode = "403", description = "Registration disabled or key invalid"),
            @ApiResponse(responseCode = "409", description = "Email already exists")})
    public AccountResponse registerAdmin(
            @RequestHeader(name = "X-Admin-Registration-Key", defaultValue = "") String registrationKey,
            @Valid @RequestBody RegistrationRequest request) {
        adminRegistrationGuard.check(registrationKey);
        return service.registerAdmin(request);
    }

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Validates credentials and ACTIVE status, then returns a signed JWT.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Authenticated"),
            @ApiResponse(responseCode = "400", description = "Validation failure"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials"),
            @ApiResponse(responseCode = "403", description = "Account suspended or disabled")})
    public LoginResponse login(@Valid @RequestBody LoginRequest request) { return service.login(request); }
}
