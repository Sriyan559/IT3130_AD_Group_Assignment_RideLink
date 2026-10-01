package com.ridelink.ride.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ridelink.ride.repository.RideRepository;
import com.ridelink.support.*;
import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import static java.util.Objects.requireNonNull;

@Component
@ConditionalOnProperty(name="ridelink.security.enabled", havingValue="true", matchIfMissing=true)
public class RideAccessFilter extends AccountAccessFilter {
    private final RideRepository rides;
    private final DriverGateway drivers;
    public RideAccessFilter(RideRepository rides, DriverGateway drivers, ObjectMapper json,
            @Value("${ridelink.account-url:http://localhost:8081}") String accountUrl,
            @Value("${ridelink.service-token:}") String token) {
        super(accountUrl, token, json); this.rides = rides; this.drivers = drivers;
    }
    @Override protected void authorize(HttpServletRequest request, Identity identity) {
        String path = request.getServletPath();
        String method = request.getMethod();
        if (identity.role().equals("ADMIN")) return;
        if (path.equals("/api/v1/rides")) {
            if (!method.equals("POST") || !identity.role().equals("PASSENGER")) denied();
            return;
        }
        String[] parts = path.split("/");
        if (parts.length < 5) { denied(); return; }
        if (parts[4].equals("passenger") && parts.length == 6) {
            identity.requireOwner(parts[5], "PASSENGER"); return;
        }
        if (parts[4].equals("driver") && parts.length == 6) {
            driverOwner(identity, parts[5]); return;
        }
        UUID rideId;
        try { rideId = UUID.fromString(parts[4]); }
        catch (IllegalArgumentException ex) { throw new AccessFailure(400, "Invalid ride ID"); }
        try {
            var ride = rides.findById(requireNonNull(rideId)).orElseThrow(() -> new AccessFailure(404, "Ride not found"));
            if (parts.length == 6 && parts[5].equals("status")) {
                driverOwner(identity, ride.getDriverId());
            } else if (method.equals("GET")) {
                if (identity.role().equals("PASSENGER")) identity.requireOwner(ride.getPassengerId(), "PASSENGER");
                else driverOwner(identity, ride.getDriverId());
            } else {
                identity.requireOwner(ride.getPassengerId(), "PASSENGER");
            }
        } catch (org.springframework.dao.DataAccessException ex) {
            throw new AccessFailure(503, "Ride database unavailable");
        }
    }
    private void driverOwner(Identity identity, String driverId) {
        if (!identity.role().equals("DRIVER") || driverId == null) denied();
        var profile = drivers.get(driverId);
        if (profile == null) throw new AccessFailure(503, "Driver lookup unavailable");
        identity.requireOwner(profile.accountId(), "DRIVER");
    }
    private void denied() { throw new AccessFailure(403, "Access denied"); }
}
