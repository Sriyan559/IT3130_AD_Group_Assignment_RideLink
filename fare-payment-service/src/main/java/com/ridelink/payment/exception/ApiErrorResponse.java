package com.ridelink.payment.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

/**
 * Consistent JSON error body returned by every Fare &amp; Payment endpoint.
 *
 * <pre>
 * {
 *   "timestamp": "2026-09-30T05:30:00Z",
 *   "status": 400,
 *   "error": "VALIDATION_ERROR",
 *   "message": "distanceKilometers must be greater than 0",
 *   "path": "/api/fares/estimate"
 * }
 * </pre>
 */
@Schema(description = "Standard error response")
public record ApiErrorResponse(
        @Schema(description = "When the error occurred (UTC)") Instant timestamp,
        @Schema(description = "HTTP status code", example = "400") int status,
        @Schema(description = "Machine-readable error code", example = "VALIDATION_ERROR") String error,
        @Schema(description = "Human-readable explanation", example = "distanceKilometers must be greater than 0") String message,
        @Schema(description = "Request path", example = "/api/fares/estimate") String path) {
}
