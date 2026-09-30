package com.ridelink.payment.exception;
import com.ridelink.support.AccessFailure;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestControllerAdvice
public class PaymentErrorHandler {
    private ResponseEntity<?> error(int status,String message) { return ResponseEntity.status(status).body(Map.of("status",status,"error",message)); }
    @ExceptionHandler(AccessFailure.class) public ResponseEntity<?> access(AccessFailure ex) { return error(ex.status(),ex.getMessage()); }
    @ExceptionHandler({org.springframework.web.bind.MethodArgumentNotValidException.class,
        org.springframework.http.converter.HttpMessageNotReadableException.class,
        org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class})
    public ResponseEntity<?> invalid(Exception ex) {return error(400,"Invalid request fields or JSON");}
    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    public ResponseEntity<?> duplicate(Exception ex) {return error(409,"Conflicting payment request; retry the same idempotency key");}
    @ExceptionHandler(org.springframework.dao.DataAccessException.class)
    public ResponseEntity<?> database(Exception ex) {return error(503,"Payment database unavailable");}
}
