package com.ridelink.ride.controller;

import io.swagger.v3.oas.annotations.Hidden;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Root informational controller.
 */
@RestController
public class RootController {

    @Value("${server.port:8083}")
    private String serverPort;

    @GetMapping("/")
    @Hidden
    public Map<String, Object> home() {
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("service", "RideLink - Ride Management Service");
        status.put("status", "UP");
        status.put("port", serverPort);
        status.put("database", "MongoDB (ride_db)");
        status.put("swaggerUi", "http://localhost:" + serverPort + "/swagger-ui.html");
        status.put("apiDocs", "http://localhost:" + serverPort + "/v3/api-docs");
        status.put("developer", "Herath H M S R (IT24103280)");
        status.put("module", "IT3130 - Application Development");
        return status;
    }
}
