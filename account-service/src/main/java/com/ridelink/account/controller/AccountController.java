package com.ridelink.account.controller;

import com.ridelink.account.dto.request.UpdateAccountRequest;
import com.ridelink.account.dto.request.UpdateAccountStatusRequest;
import com.ridelink.account.dto.response.AccountResponse;
import com.ridelink.account.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {
    private final AccountService service;
    public AccountController(AccountService service) { this.service = service; }

    @GetMapping("/{id}")
    @PreAuthorize("@accountAuthorization.canAccess(#id, authentication)")
    @Operation(summary = "Get account profile", description = "Accessible by the account owner or ADMIN.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Profile returned"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT"),
            @ApiResponse(responseCode = "403", description = "Not the owner or ADMIN"),
            @ApiResponse(responseCode = "404", description = "Account not found")})
    public AccountResponse get(@PathVariable String id) { return service.get(id); }

    @PutMapping("/{id}")
    @PreAuthorize("@accountAuthorization.canAccess(#id, authentication)")
    @Operation(summary = "Update account profile", description = "Updates only fullName and phone; role, status, email and credentials cannot be changed here.")
    public AccountResponse update(@PathVariable String id, @Valid @RequestBody UpdateAccountRequest request) {
        return service.update(id, request);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update account status", description = "ADMIN-only transition to ACTIVE, SUSPENDED or DISABLED.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Status updated"),
            @ApiResponse(responseCode = "400", description = "Invalid status"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT"),
            @ApiResponse(responseCode = "403", description = "ADMIN role required"),
            @ApiResponse(responseCode = "404", description = "Account not found")})
    public AccountResponse status(@PathVariable String id, @Valid @RequestBody UpdateAccountStatusRequest request) {
        return service.updateStatus(id, request.status());
    }
}
