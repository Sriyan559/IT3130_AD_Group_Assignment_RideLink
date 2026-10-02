package com.ridelink.payment.exception;

import org.springframework.http.HttpStatus;

/**
 * Business or integration failure that maps directly to an HTTP status and error code.
 * <p>
 * Services throw this instead of building HTTP responses themselves, so business logic
 * stays independent of the web layer; {@link GlobalExceptionHandler} converts it to
 * an {@link ApiErrorResponse}.
 */
public class ApiException extends RuntimeException {

    private final HttpStatus status;
    private final String errorCode;

    public ApiException(HttpStatus status, String errorCode, String message) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
    }

    public static ApiException badRequest(String errorCode, String message) {
        return new ApiException(HttpStatus.BAD_REQUEST, errorCode, message);
    }

    public static ApiException notFound(String errorCode, String message) {
        return new ApiException(HttpStatus.NOT_FOUND, errorCode, message);
    }

    public static ApiException conflict(String errorCode, String message) {
        return new ApiException(HttpStatus.CONFLICT, errorCode, message);
    }

    public static ApiException unavailable(String errorCode, String message) {
        return new ApiException(HttpStatus.SERVICE_UNAVAILABLE, errorCode, message);
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
