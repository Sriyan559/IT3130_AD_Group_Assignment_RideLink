package com.ridelink.payment.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI 3 / Swagger documentation configuration for Fare & Payment Service.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI farePaymentServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("RideLink - Fare & Payment Service API")
                        .description("API specification for fare estimates, final fare calculations, simulated payments, and payment receipts.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Sanjeewa H.D.U.S (IT24101590)")
                                .email("it24101590@my.sliit.lk"))
                        .license(new License().name("Academic Use - IT3130")));
    }
}
