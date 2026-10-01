package com.ridelink.ride.integration;

import com.ridelink.support.AccessFailure;
import java.time.Duration;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.*;
import static java.util.Objects.requireNonNull;
@Component
public class PaymentGateway {
    public record Result(UUID id,UUID receiptId,String status) {}
    private final RestTemplate http=new RestTemplateBuilder().setConnectTimeout(Duration.ofSeconds(2)).setReadTimeout(Duration.ofSeconds(5)).build();
    private final String url,token;
    public PaymentGateway(@Value("${services.payment.url:http://localhost:8084}") String url,
            @Value("${ridelink.service-token:}") String token) {this.url=url;this.token=token;}
    public Result process(UUID rideId,boolean simulateFailure) {
        if(token.length()<32) throw new AccessFailure(503,"Payment service credentials not configured");
        HttpHeaders headers=new HttpHeaders();headers.set("X-Service-Token",token);
        var body=Map.of("rideId",rideId,"idempotencyKey","completion-"+rideId,"paymentMethod","CARD","simulateFailure",simulateFailure);
        try {
            Result result=http.exchange(url+"/internal/payments/process",requireNonNull(HttpMethod.POST),new HttpEntity<>(body,headers),Result.class).getBody();
            if(result==null || result.id()==null) throw new AccessFailure(503,"Invalid payment response");
            return result;
        } catch(HttpClientErrorException.Conflict ex) {
            try {
                Result paid=http.exchange(url+"/internal/payments/ride/{id}/successful",requireNonNull(HttpMethod.GET),
                        new HttpEntity<>(headers),Result.class,rideId).getBody();
                if(paid!=null && paid.id()!=null && "SUCCESS".equals(paid.status())) return paid;
            } catch(RestClientException ignored) { }
            throw new AccessFailure(503,"Payment processing pending; retry the same ride");
        } catch(RestClientException ex) {throw new AccessFailure(503,"Payment processing pending; retry the same ride");}
    }
}
