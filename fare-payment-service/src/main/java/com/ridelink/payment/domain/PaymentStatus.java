package com.ridelink.payment.domain;

/**
 * Outcome of a simulated payment attempt.
 * <p>
 * A simulated payment completes immediately, so an attempt is stored as either SUCCESS or FAILED.
 * Only SUCCESS attempts get a receipt; a FAILED attempt can be retried with a new idempotency key.
 */
public enum PaymentStatus {
    SUCCESS,
    FAILED
}
