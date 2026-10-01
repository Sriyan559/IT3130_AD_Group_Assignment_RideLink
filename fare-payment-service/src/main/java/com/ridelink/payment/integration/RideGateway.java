package com.ridelink.payment.integration;

import com.ridelink.support.AccessFailure;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.*;
import static java.util.Objects.requireNonNull;
@Component
public class RideGateway {
    public record Trip(UUID id, String passengerId, String status, BigDecimal distanceKm, Long durationMinutes) {}
    private final RestTemplate http=new RestTemplateBuilder().setConnectTimeout(Duration.ofSeconds(2)).setReadTimeout(Duration.ofSeconds(3)).build();
    private final String url, token;
    public RideGateway(@Value("${services.ride.url:http://localhost:8083}") String url,
            @Value("${ridelink.service-token:}") String token) { this.url=url; this.token=token; }
    public Trip get(UUID id) {
        if(token.length()<32) throw new AccessFailure(503,"Ride service credentials not configured");
        HttpHeaders headers=new HttpHeaders(); headers.set("X-Service-Token",token);
        try {
            Trip trip=http.exchange(url+"/internal/rides/{id}",requireNonNull(HttpMethod.GET),new HttpEntity<>(headers),Trip.class,id).getBody();
            if(trip==null) throw new AccessFailure(503,"Invalid Ride response");
            return trip;
        } catch(HttpClientErrorException ex) {
            throw new AccessFailure(ex.getStatusCode().value()==404?404:503,"Ride lookup failed");
        } catch(RestClientException ex) { throw new AccessFailure(503,"Ride service unavailable"); }
    }
}
