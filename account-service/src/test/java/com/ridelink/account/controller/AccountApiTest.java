package com.ridelink.account.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ridelink.account.config.SecurityConfig;
import com.ridelink.account.domain.AccountStatus;
import com.ridelink.account.domain.Role;
import com.ridelink.account.dto.response.AccountResponse;
import com.ridelink.account.dto.response.LoginResponse;
import com.ridelink.account.exception.GlobalExceptionHandler;
import com.ridelink.account.repository.AccountRepository;
import com.ridelink.account.security.AccountAuthorization;
import com.ridelink.account.security.JwtAuthenticationFilter;
import com.ridelink.account.security.JwtService;
import com.ridelink.account.security.RestAccessDeniedHandler;
import com.ridelink.account.security.RestAuthenticationEntryPoint;
import com.ridelink.account.service.AccountService;
import com.ridelink.account.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {AuthController.class, AccountController.class})
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, RestAuthenticationEntryPoint.class,
        RestAccessDeniedHandler.class, AccountAuthorization.class, GlobalExceptionHandler.class})
@SuppressWarnings("null")
class AccountApiTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean AuthService authService;
    @MockBean AccountService accountService;
    @MockBean JwtService jwtService;
    @MockBean AccountRepository accountRepository;

    @Test @WithMockUser(username="p1", roles="PASSENGER")
    void meUsesAuthenticatedIdentity() throws Exception {
        when(accountService.get("p1")).thenReturn(response("p1", Role.PASSENGER));
        mvc.perform(get("/api/accounts/me")).andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("p1"));
    }

    @Test void publicRegistrationEndpoint_andPasswordHashNeverExposed() throws Exception {
        when(authService.registerPassenger(any())).thenReturn(response("p1", Role.PASSENGER));
        mvc.perform(post("/api/auth/register/passenger").contentType("application/json")
                        .content("{\"fullName\":\"Person\",\"email\":\"p@example.com\",\"password\":\"password1\",\"phone\":\"+94771234567\"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.id").value("p1"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test void publicLoginEndpoint() throws Exception {
        when(authService.login(any())).thenReturn(new LoginResponse("jwt", "Bearer", 3600000, response("p1", Role.PASSENGER)));
        mvc.perform(post("/api/auth/login").contentType("application/json")
                        .content("{\"email\":\"p@example.com\",\"password\":\"password1\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.token").value("jwt"));
    }

    @Test void profileUpdateValidation() throws Exception {
        mvc.perform(put("/api/accounts/p1").with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("p1").roles("PASSENGER"))
                        .contentType("application/json").content("{\"fullName\":\"\",\"phone\":\"bad\"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test void protectedAccountEndpointWithoutToken_returns401() throws Exception {
        mvc.perform(get("/api/accounts/p1")).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"));
    }

    @Test void protectedEndpointWithInvalidToken_returns401() throws Exception {
        when(jwtService.parse("invalid")).thenThrow(new io.jsonwebtoken.MalformedJwtException("invalid"));
        mvc.perform(get("/api/accounts/p1").header("Authorization", "Bearer invalid"))
                .andExpect(status().isUnauthorized());
    }

    @Test @WithMockUser(username = "p1", roles = "PASSENGER")
    void passengerStatusUpdate_returns403() throws Exception {
        mvc.perform(patch("/api/accounts/p1/status").contentType("application/json").content("{\"status\":\"SUSPENDED\"}"))
                .andExpect(status().isForbidden());
    }

    @Test @WithMockUser(username = "d1", roles = "DRIVER")
    void driverStatusUpdate_returns403() throws Exception {
        mvc.perform(patch("/api/accounts/p1/status").contentType("application/json").content("{\"status\":\"SUSPENDED\"}"))
                .andExpect(status().isForbidden());
    }

    @Test @WithMockUser(username = "admin", roles = "ADMIN")
    void adminStatusUpdate_allowed() throws Exception {
        when(accountService.updateStatus(eq("p1"), eq(AccountStatus.SUSPENDED))).thenReturn(response("p1", Role.PASSENGER));
        mvc.perform(patch("/api/accounts/p1/status").contentType("application/json").content("{\"status\":\"SUSPENDED\"}"))
                .andExpect(status().isOk());
    }

    private AccountResponse response(String id, Role role) {
        return new AccountResponse(id, "Person", "p@example.com", role, "+94771234567",
                AccountStatus.ACTIVE, Instant.now(), Instant.now());
    }
}
