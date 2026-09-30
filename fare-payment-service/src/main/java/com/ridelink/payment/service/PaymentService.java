package com.ridelink.payment.service;
import com.ridelink.payment.entity.*;
import com.ridelink.payment.dto.request.ProcessPaymentRequest;
import com.ridelink.payment.repository.*;
import com.ridelink.support.AccessFailure;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
public class PaymentService {
    private final FinalFareRepository fares; private final PaymentRepository payments; private final ReceiptRepository receipts;
    public PaymentService(FinalFareRepository fares,PaymentRepository payments,ReceiptRepository receipts) {
        this.fares=fares;this.payments=payments;this.receipts=receipts;
    }
    @Transactional
    public PaymentAttempt process(ProcessPaymentRequest request) {
        FinalFare fare=fares.lockByRideId(request.rideId()).orElseThrow(()->new AccessFailure(404,"Final fare missing"));
        var previous=payments.findByIdempotencyKey(request.idempotencyKey());
        if(previous.isPresent()) {
            PaymentAttempt old=previous.get();
            if(!old.rideId.equals(request.rideId()) || !old.paymentMethod.equals(request.paymentMethod().name())
                    || old.simulateFailure!=request.simulateFailure()) throw new AccessFailure(409,"Idempotency key belongs to a different request");
            return old;
        }
        if(fare.paidPaymentId!=null) throw new AccessFailure(409,"Ride already paid");
        PaymentAttempt attempt=new PaymentAttempt();attempt.id=UUID.randomUUID();attempt.rideId=fare.rideId;
        attempt.passengerId=fare.passengerId;attempt.idempotencyKey=request.idempotencyKey();attempt.amount=fare.total;
        attempt.paymentMethod=request.paymentMethod().name();attempt.simulateFailure=request.simulateFailure();
        attempt.status=request.simulateFailure()?"FAILED":"SUCCESS";attempt.transactionReference="SIM-"+attempt.id;
        attempt.createdAt=Instant.now();
        if(!request.simulateFailure()) { attempt.paidAt=attempt.createdAt;attempt.receiptId=UUID.randomUUID(); }
        payments.saveAndFlush(attempt);
        if(attempt.receiptId!=null) {
            Receipt receipt=new Receipt();receipt.id=attempt.receiptId;receipt.paymentId=attempt.id;receipt.rideId=fare.rideId;
            receipt.passengerId=fare.passengerId;receipt.baseFare=fare.baseFare;receipt.distanceFare=fare.distanceFare;
            receipt.timeFare=fare.timeFare;receipt.minimumAdjustment=fare.minimumAdjustment;receipt.total=fare.total;
            receipt.transactionReference=attempt.transactionReference;receipt.issuedAt=attempt.paidAt;
            receipts.saveAndFlush(receipt);fare.paidPaymentId=attempt.id;fares.save(fare);
        }
        return attempt;
    }
}
