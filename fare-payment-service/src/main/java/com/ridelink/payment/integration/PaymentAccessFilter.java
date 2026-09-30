package com.ridelink.payment.integration;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ridelink.support.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
@Component
public class PaymentAccessFilter extends AccountAccessFilter {
    public PaymentAccessFilter(ObjectMapper json,
            @Value("${ridelink.account-url:http://localhost:8081}") String url,
            @Value("${ridelink.service-token:}") String token) { super(url,token,json); }
    @Override protected void authorize(HttpServletRequest request, Identity identity) {
        if(!identity.role().equals("PASSENGER") && !identity.role().equals("ADMIN"))
            throw new AccessFailure(403,"Passenger account required");
    }
}
