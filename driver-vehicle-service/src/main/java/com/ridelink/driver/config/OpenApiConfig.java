package com.ridelink.driver.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI 3 / Swagger documentation configuration for Driver & Vehicle Service.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI driverVehicleServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("RideLink - Driver & Vehicle Service API")
                        .description("API specification for driver operational status, vehicle data, availability, and location simulation.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Rathnakoon D A (IT24300246)")
                                .email("it24300246@my.sliit.lk"))
                        .license(new License().name("Academic Use - IT3130")));
    }
}
