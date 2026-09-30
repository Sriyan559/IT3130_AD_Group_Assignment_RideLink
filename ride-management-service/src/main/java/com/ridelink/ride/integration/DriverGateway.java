package com.ridelink.ride.integration;

import com.ridelink.support.AccessFailure;
import java.time.Duration;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.*;

@Component
public class DriverGateway {
    private final RestTemplate http = new RestTemplateBuilder().setConnectTimeout(Duration.ofSeconds(2))
            .setReadTimeout(Duration.ofSeconds(3)).build();
    private final String url;
    private final String token;
    public record Profile(String id, String accountId, String availabilityStatus) {}
    public record Candidate(String driverId) {}
    public java.util.List<Candidate> eligible(java.math.BigDecimal lat,java.math.BigDecimal lng,java.math.BigDecimal radius) {
        if(token.length()<32) throw new AccessFailure(503,"Driver service credentials not configured");
        HttpHeaders headers=new HttpHeaders();headers.set("X-Service-Token",token);
        try {
            Candidate[] matches=http.exchange(url+"/internal/drivers/eligible?lat={lat}&lng={lng}&radius={radius}",
                    HttpMethod.GET,new HttpEntity<>(headers),Candidate[].class,lat,lng,radius).getBody();
            return matches==null?java.util.List.of():java.util.Arrays.asList(matches);
        } catch(RestClientException ex) {throw new AccessFailure(503,"Driver search unavailable");}
    }
    public DriverGateway(@Value("${ridelink.driver-url:http://localhost:8082}") String url,
            @Value("${ridelink.service-token:}") String token) { this.url = url; this.token = token; }
    public Profile get(String driverId) { return call(HttpMethod.GET, "/internal/drivers/{driverId}", driverId); }
    public void reserve(String driverId, UUID rideId) {
        call(HttpMethod.PUT, "/internal/drivers/{driverId}/reservations/{rideId}", driverId, rideId);
    }
    public void release(String driverId, UUID rideId) {
        call(HttpMethod.DELETE, "/internal/drivers/{driverId}/reservations/{rideId}", driverId, rideId);
    }
    private Profile call(HttpMethod method, String path, Object... variables) {
        if (token.length() < 32) throw new AccessFailure(503, "Driver service credentials not configured");
        HttpHeaders headers = new HttpHeaders(); headers.set("X-Service-Token", token);
        try {
            return http.exchange(url + path, method, new HttpEntity<>(headers), Profile.class, variables).getBody();
        } catch (HttpClientErrorException ex) {
            int status = ex.getStatusCode().value();
            throw new AccessFailure(status == 404 || status == 409 ? status : 503,
                    status == 409 ? "Driver reservation conflict" : status == 404 ? "Driver not found" : "Driver service authentication failed");
        } catch (RestClientException ex) {
            throw new AccessFailure(503, "Driver service unavailable; retry with the same ride ID");
        }
    }
}
