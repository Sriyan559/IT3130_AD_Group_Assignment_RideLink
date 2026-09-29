package com.ridelink.driver.integration;
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
}
