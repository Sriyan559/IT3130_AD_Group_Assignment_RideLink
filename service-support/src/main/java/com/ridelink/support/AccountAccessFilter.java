package com.ridelink.support;

import org.springframework.lang.NonNull;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.Map;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.web.client.*;
import org.springframework.web.filter.OncePerRequestFilter;
import static java.util.Objects.requireNonNull;

/** Account Service validates signature, current role and ACTIVE status on every request. */
public abstract class AccountAccessFilter extends OncePerRequestFilter {
    private final RestTemplate http = new RestTemplateBuilder().setConnectTimeout(Duration.ofSeconds(2))
            .setReadTimeout(Duration.ofSeconds(3)).build();
    private final String accountUrl;
    private final String serviceToken;
    private final ObjectMapper json;
    protected AccountAccessFilter(String accountUrl, String serviceToken, ObjectMapper json) {
        this.accountUrl = accountUrl;
        this.serviceToken = serviceToken;
        this.json = json;
    }
    @Override protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String path = request.getServletPath();
        return !(path.startsWith("/api/") || path.startsWith("/internal/"));
    }
    @Override protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
            @NonNull FilterChain chain) throws ServletException, IOException {
        try {
            if (request.getServletPath().startsWith("/internal/")) {
                String supplied = request.getHeader("X-Service-Token");
                if (serviceToken.length() < 32 || supplied == null || !MessageDigest.isEqual(
                        serviceToken.getBytes(StandardCharsets.UTF_8), supplied.getBytes(StandardCharsets.UTF_8)))
                    throw new AccessFailure(401, "Service authentication required");
            } else {
                String token = request.getHeader(HttpHeaders.AUTHORIZATION);
                if (token == null || !token.startsWith("Bearer ") || token.length() <= 7)
                    throw new AccessFailure(401, "Bearer token required");
                HttpHeaders headers = new HttpHeaders();
                headers.set(HttpHeaders.AUTHORIZATION, token);
                Identity identity;
                try {
                    identity = http.exchange(accountUrl + "/api/accounts/me", requireNonNull(HttpMethod.GET),
                            new HttpEntity<>(headers), Identity.class).getBody();
                } catch (HttpClientErrorException ex) {
                    throw new AccessFailure(ex.getStatusCode().value() == 403 ? 403 : 401, "Invalid or inactive account");
                } catch (RestClientException ex) {
                    throw new AccessFailure(503, "Account verification unavailable");
                }
                if (identity == null || identity.id() == null || identity.role() == null || !"ACTIVE".equals(identity.status()))
                    throw new AccessFailure(401, "Invalid or inactive account");
                request.setAttribute(Identity.class.getName(), identity);
                authorize(request, identity);
            }
        } catch (AccessFailure ex) {
            response.setStatus(ex.status());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            json.writeValue(response.getOutputStream(), Map.of("status", ex.status(), "error", ex.getMessage()));
            return;
        }
        chain.doFilter(request, response);
    }
    protected abstract void authorize(HttpServletRequest request, Identity identity);
}
