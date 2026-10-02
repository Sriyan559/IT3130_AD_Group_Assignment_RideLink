package com.ridelink.payment.config;

import java.time.Clock;
import java.time.Duration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/** Infrastructure beans. Declared here so tests can replace them with fakes. */
@Configuration
public class AppConfig {

    /** HTTP client for Ride Management Service. Short timeouts stop a slow Ride Service from blocking payments. */
    @Bean
    public RestTemplate rideRestTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofSeconds(2))
                .setReadTimeout(Duration.ofSeconds(3))
                .build();
    }

    /** Source of "now" for timestamps; a fixed clock can be injected in tests. */
    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
