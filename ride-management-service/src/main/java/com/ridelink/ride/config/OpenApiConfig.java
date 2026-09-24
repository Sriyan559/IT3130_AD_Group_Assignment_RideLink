package com.ridelink.ride.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI 3 / Swagger documentation configuration for Ride Management Service.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI rideManagementServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("RideLink - Ride Management Service API")
                        .description("API specification for ride booking, status lifecycle transitions, driver assignment, and trip tracking.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Herath H M S R (IT24103280)")
                                .email("it24103280@my.sliit.lk"))
                        .license(new License().name("Academic Use - IT3130")));
    }
}
