package com.ridelink.account.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI 3 / Swagger documentation configuration for Account Service.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI accountServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("RideLink - Account Service API")
                        .description("API specification for user accounts, authentication, role management, and profile operations.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Fernando B S C (IT24103775)")
                                .email("it24103775@my.sliit.lk"))
                        .license(new License().name("Academic Use - IT3130")));
    }
}
