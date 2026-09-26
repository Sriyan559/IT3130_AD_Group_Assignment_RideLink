package com.ridelink.driver.exception;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.stream.Collectors;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {
    public record ApiError(Instant timestamp, int status, String error, String message, String path) { }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> validation(MethodArgumentNotValidException exception,
                                                HttpServletRequest request) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .sorted().collect(Collectors.joining("; "));
        return error(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", message, request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> malformed(HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "A valid JSON request body is required", request);
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<ApiError> duplicate(HttpServletRequest request) {
        return error(HttpStatus.CONFLICT, "DRIVER_ALREADY_EXISTS",
                "A driver with this account ID or licence number already exists", request);
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ApiError> database(HttpServletRequest request) {
        return error(HttpStatus.SERVICE_UNAVAILABLE, "DATABASE_UNAVAILABLE",
                "Driver data is temporarily unavailable", request);
    }

    @ExceptionHandler(DriverNotFoundException.class)
    public ResponseEntity<ApiError> driverNotFound(HttpServletRequest request) {
        return error(HttpStatus.NOT_FOUND, "DRIVER_NOT_FOUND", "Driver profile not found", request);
    }

    @ExceptionHandler(VehicleAlreadyExistsException.class)
    public ResponseEntity<ApiError> vehicleDuplicate(HttpServletRequest request) {
        return error(HttpStatus.CONFLICT, "VEHICLE_ALREADY_EXISTS",
                "A vehicle with this plate number already exists", request);
    }

    private ResponseEntity<ApiError> error(HttpStatus status, String code, String message,
                                           HttpServletRequest request) {
        return ResponseEntity.status(status).body(new ApiError(Instant.now(), status.value(),
                code, message, request.getRequestURI()));
    }
}
