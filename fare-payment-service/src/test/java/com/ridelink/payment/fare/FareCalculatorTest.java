package com.ridelink.payment.fare;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;
class FareCalculatorTest {
    FareCalculator calc=new FareCalculator(new BigDecimal("150"),new BigDecimal("100"),new BigDecimal("10"),new BigDecimal("200"));
    @Test void distanceAndDurationTariff() {
        var fare=calc.calculate(new BigDecimal("8"),20L);
        assertThat(fare.total()).isEqualByComparingTo("1150.00");
        assertThat(fare.currency()).isEqualTo("LKR");
    }
    @Test void minimumAndHalfUpRounding() {
        assertThat(calc.calculate(new BigDecimal("0.01"),0L).total()).isEqualByComparingTo("200.00");
        assertThat(calc.calculate(new BigDecimal("1.23455"),1L).total()).isEqualByComparingTo("283.46");
    }
    @Test void invalidMetricsRejected() {
        for(String distance:new String[]{"0","-1","10001"})
            assertThatThrownBy(()->calc.calculate(new BigDecimal(distance),1L)).isInstanceOf(com.ridelink.support.AccessFailure.class);
        assertThatThrownBy(()->calc.calculate(BigDecimal.ONE,-1L)).isInstanceOf(com.ridelink.support.AccessFailure.class);
        assertThatThrownBy(()->calc.calculate(BigDecimal.ONE,null)).isInstanceOf(com.ridelink.support.AccessFailure.class);
    }
}
