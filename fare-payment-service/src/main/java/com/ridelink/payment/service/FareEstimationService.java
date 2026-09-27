package com.ridelink.payment.service;

import com.ridelink.payment.dto.request.FareEstimateRequest;
import com.ridelink.payment.dto.response.FareEstimateResponse;
import com.ridelink.payment.fare.FareCalculator;
import org.springframework.stereotype.Service;

@Service
public class FareEstimationService {

    public FareEstimateResponse estimate(FareEstimateRequest request) {
        return new FareEstimateResponse(
                request.distanceKilometers(),
                FareCalculator.calculateFare(request.distanceKilometers()),
                "LKR");
    }
}