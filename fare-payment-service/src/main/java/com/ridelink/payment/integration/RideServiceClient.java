package com.ridelink.payment.integration;

import com.ridelink.payment.exception.ApiException;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * Synchronous REST client for Ride Management Service.
 * <p>
 * The final fare must be based on the trip Ride Service recorded (status, distance, duration),
 * not on numbers the passenger sends. Fare &amp; Payment never reads the ride database directly;
 * it calls Ride's internal API with the shared service token.
 */
@Component
public class RideServiceClient {

    static final String SERVICE_TOKEN_HEADER = "X-Service-Token";
    static final int MIN_TOKEN_LENGTH = 32;

    private final RestTemplate restTemplate;
    private final String rideServiceUrl;
    private final String serviceToken;

    public RideServiceClient(RestTemplate rideRestTemplate,
                             @Value("${services.ride.url:http://localhost:8083}") String rideServiceUrl,
                             @Value("${ridelink.service-token:}") String serviceToken) {
        this.restTemplate = rideRestTemplate;
        this.rideServiceUrl = rideServiceUrl;
        this.serviceToken = serviceToken;
    }

    public RideSnapshot getRide(UUID rideId) {
        if (serviceToken.length() < MIN_TOKEN_LENGTH) {
            throw ApiException.unavailable("RIDE_SERVICE_UNAVAILABLE", "Ride service credentials are not configured");
        }
        HttpHeaders headers = new HttpHeaders();
        headers.set(SERVICE_TOKEN_HEADER, serviceToken);
        try {
            RideSnapshot ride = restTemplate.exchange(rideServiceUrl + "/internal/rides/{id}", HttpMethod.GET,
                    new HttpEntity<>(headers), RideSnapshot.class, rideId).getBody();
            if (ride == null || ride.id() == null) {
                throw ApiException.unavailable("RIDE_SERVICE_UNAVAILABLE", "Ride service returned an empty response");
            }
            return ride;
        } catch (HttpClientErrorException ex) {
            if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw ApiException.notFound("RIDE_NOT_FOUND", "Ride " + rideId + " does not exist");
            }
            throw ApiException.unavailable("RIDE_SERVICE_UNAVAILABLE", "Ride lookup was rejected by Ride service");
        } catch (RestClientException ex) {
            throw ApiException.unavailable("RIDE_SERVICE_UNAVAILABLE", "Ride service is unavailable; try again later");
        }
    }
}
