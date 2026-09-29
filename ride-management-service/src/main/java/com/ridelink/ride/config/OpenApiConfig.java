package com.ridelink.ride.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI 3 / Swagger documentation configuration for Ride Management Service.
 */
@Configuration
public class OpenApiConfig {

    @Value("${server.port:8083}")
    private String serverPort;

    @Bean
    public OpenAPI rideManagementServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("RideLink - Ride Management Service API")
                        .description("API specification for ride booking, status lifecycle transitions, driver assignment, passenger history, and trip tracking.\n\n"
                                + "### Project Details\n"
                                + "- **Primary Owner:** Herath H M S R (Student ID: IT24103280)\n"
                                + "- **Module:** IT3130 - Application Development\n"
                                + "- **Database:** MongoDB (`ride_db`)\n"
                                + "- **Service Port:** " + serverPort + "\n\n"
                                + "### State Machine Transitions\n"
                                + "- `REQUESTED` -> `ASSIGNED` or `CANCELLED`\n"
                                + "- `ASSIGNED` -> `ACCEPTED` or `CANCELLED`\n"
                                + "- `ACCEPTED` -> `IN_PROGRESS` or `CANCELLED`\n"
                                + "- `IN_PROGRESS` -> `COMPLETED`\n"
                                + "- `COMPLETED` / `CANCELLED` (Terminal states)")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Herath H M S R (IT24103280)")
                                .email("it24103280@my.sliit.lk"))
                        .license(new License().name("Academic Use - IT3130")))
                .servers(List.of(
                        new Server().url("http://localhost:" + serverPort).description("Local Development Server")
                ));
    }
}
