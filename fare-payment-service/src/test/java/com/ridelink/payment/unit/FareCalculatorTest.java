package com.ridelink.payment.unit;

import com.ridelink.payment.fare.FareCalculator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FareCalculatorTest {

    @Test
    void calculatesFareFromBaseFareAndDistance() {
        assertEquals(new BigDecimal("950.00"), FareCalculator.calculateFare(new BigDecimal("8")));
    }

    @Test
    void roundsFareToTwoDecimalPlaces() {
        assertEquals(new BigDecimal("150.01"), FareCalculator.calculateFare(new BigDecimal("0.00005")));
    }

    @Test
    void rejectsNullDistance() {
        assertThrows(IllegalArgumentException.class, () -> FareCalculator.calculateFare(null));
    }

    @Test
    void rejectsZeroOrNegativeDistance() {
        assertThrows(IllegalArgumentException.class, () -> FareCalculator.calculateFare(BigDecimal.ZERO));
        assertThrows(IllegalArgumentException.class, () -> FareCalculator.calculateFare(new BigDecimal("-1")));
    }
}