package com.ridelink.driver.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ridelink.driver.repository.DriverRepository;
import com.ridelink.support.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import static java.util.Objects.requireNonNull;

@Component
@ConditionalOnProperty(name="ridelink.security.enabled", havingValue="true", matchIfMissing=true)
public class DriverAccessFilter extends AccountAccessFilter {
    private final DriverRepository drivers;
    public DriverAccessFilter(DriverRepository drivers, ObjectMapper json,
            @Value("${ridelink.account-url:http://localhost:8081}") String accountUrl,
            @Value("${ridelink.service-token:}") String token) {
        super(accountUrl, token, json);
        this.drivers = drivers;
    }
    @Override protected void authorize(HttpServletRequest request, Identity identity) {
        String path = request.getServletPath();
        if (path.equals("/api/drivers/eligible")) return;
        if (!identity.role().equals("DRIVER")) throw new AccessFailure(403, "Driver account required");
        String[] parts = path.split("/");
        if (parts.length >= 4 && parts[2].equals("drivers")) {
            try {
                var driver = drivers.findById(requireNonNull(parts[3])).orElseThrow(() -> new AccessFailure(404, "Driver not found"));
                identity.requireOwner(driver.accountId(), "DRIVER");
            } catch (org.springframework.dao.DataAccessException ex) {
                throw new AccessFailure(503, "Driver database unavailable");
            }
        }
    }
}
