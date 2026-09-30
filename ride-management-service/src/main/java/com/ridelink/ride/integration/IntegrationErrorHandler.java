package com.ridelink.ride.integration;
import com.ridelink.support.AccessFailure;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestControllerAdvice
public class IntegrationErrorHandler {
    @ExceptionHandler(AccessFailure.class)
    public ResponseEntity<?> access(AccessFailure ex) {
        return ResponseEntity.status(ex.status()).body(Map.of("status", ex.status(), "error", ex.getMessage()));
    }
    @ExceptionHandler(org.springframework.dao.OptimisticLockingFailureException.class)
    public ResponseEntity<?> concurrent(Exception ex) {
        return ResponseEntity.status(409).body(Map.of("status", 409, "error", "Ride changed concurrently; reload and retry"));
    }
    @ExceptionHandler(org.springframework.dao.DataAccessException.class)
    public ResponseEntity<?> database(Exception ex) {
        return ResponseEntity.status(503).body(Map.of("status", 503, "error", "Ride database unavailable"));
    }
}
