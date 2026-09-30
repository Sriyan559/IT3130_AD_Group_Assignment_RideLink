package com.ridelink.payment.controller;
import com.ridelink.payment.dto.request.ProcessPaymentRequest;
import com.ridelink.payment.entity.*;
import com.ridelink.payment.fare.FareCalculator;
import com.ridelink.payment.integration.RideGateway;
import com.ridelink.payment.repository.*;
import com.ridelink.payment.service.*;
import com.ridelink.support.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.*;

@RestController
public class PaymentController {
    public record Estimate(@NotNull @DecimalMin(value="0",inclusive=false) @DecimalMax("10000") BigDecimal distanceKilometers,
            @NotNull @Min(0) @Max(100000) Long durationMinutes) {}
    public record FinalRequest(@NotNull UUID rideId) {}
    private final FareCalculator calculator; private final RideGateway rides; private final FinalFareService finalizer;
    private final PaymentService processor; private final FinalFareRepository fares;
    private final PaymentRepository payments; private final ReceiptRepository receipts;
    public PaymentController(FareCalculator calculator,RideGateway rides,FinalFareService finalizer,PaymentService processor,
            FinalFareRepository fares,PaymentRepository payments,ReceiptRepository receipts) {
        this.calculator=calculator;this.rides=rides;this.finalizer=finalizer;this.processor=processor;
        this.fares=fares;this.payments=payments;this.receipts=receipts;
    }
    private void owner(String id) {
        Identity who=Identity.current();
        if(!who.role().equals("ADMIN")) who.requireOwner(id,"PASSENGER");
    }
    @PostMapping({"/api/v1/fare/estimate","/api/fares/estimate"})
    public FareCalculator.Breakdown estimate(@Valid @RequestBody Estimate request) {
        return calculator.calculate(request.distanceKilometers(),request.durationMinutes());
    }
    private FinalFare ensure(UUID id,boolean internal) {
        var trip=rides.get(id); if(!internal) owner(trip.passengerId());
        try { return finalizer.ensure(trip); }
        catch(DataIntegrityViolationException ex) {
            // The competing transaction may have inserted this immutable fare first.
            return fares.findById(id).orElseThrow(()->new AccessFailure(409,"Concurrent fare creation; retry same ride"));
        }
    }
    @PostMapping({"/api/v1/fare/final","/api/fares/final"})
    public FinalFare finalizeFare(@Valid @RequestBody FinalRequest request) { return ensure(request.rideId(),false); }
    @GetMapping({"/api/v1/fare/final/{rideId}","/api/fares/final/{rideId}"})
    public FinalFare getFare(@PathVariable UUID rideId) {
        var result=fares.findById(rideId).orElseThrow(()->new AccessFailure(404,"Final fare not found"));owner(result.passengerId);return result;
    }
    @PostMapping({"/api/v1/payments/process","/api/payments"})
    public PaymentAttempt process(@Valid @RequestBody ProcessPaymentRequest request) {
        ensure(request.rideId(),false);return processor.process(request);
    }
    @io.swagger.v3.oas.annotations.Hidden
    @PostMapping("/internal/payments/process")
    public PaymentAttempt internal(@Valid @RequestBody ProcessPaymentRequest request) {
        ensure(request.rideId(),true);return processor.process(request);
    }
    @io.swagger.v3.oas.annotations.Hidden
    @GetMapping("/internal/payments/ride/{rideId}/successful")
    public PaymentAttempt successful(@PathVariable UUID rideId) {
        FinalFare fare=fares.findById(rideId).orElseThrow(()->new AccessFailure(404,"Fare not found"));
        if(fare.paidPaymentId==null) throw new AccessFailure(404,"Successful payment not found");
        return payments.findById(fare.paidPaymentId).orElseThrow(()->new AccessFailure(404,"Payment not found"));
    }
    @GetMapping({"/api/v1/payments/{id}","/api/payments/{id}"})
    public PaymentAttempt getPayment(@PathVariable UUID id) {
        var result=payments.findById(id).orElseThrow(()->new AccessFailure(404,"Payment not found"));owner(result.passengerId);return result;
    }
    @GetMapping("/api/v1/payments/ride/{rideId}")
    public List<PaymentAttempt> history(@PathVariable UUID rideId) {
        owner(rides.get(rideId).passengerId());return payments.findByRideIdOrderByCreatedAtAsc(rideId);
    }
    @GetMapping("/api/v1/receipts/{id}")
    public Receipt receipt(@PathVariable UUID id) {
        var result=receipts.findById(id).orElseThrow(()->new AccessFailure(404,"Receipt not found"));owner(result.passengerId);return result;
    }
    @GetMapping("/api/v1/receipts/ride/{rideId}")
    public Receipt receiptForRide(@PathVariable UUID rideId) {
        var result=receipts.findByRideId(rideId).orElseThrow(()->new AccessFailure(404,"Receipt not found"));owner(result.passengerId);return result;
    }
}
