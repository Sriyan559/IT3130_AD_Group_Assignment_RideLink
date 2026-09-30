package com.ridelink.payment.payment;
import com.ridelink.payment.dto.request.ProcessPaymentRequest;
import com.ridelink.payment.dto.request.ProcessPaymentRequest.Method;
import com.ridelink.payment.integration.RideGateway.Trip;
import com.ridelink.payment.repository.*;
import com.ridelink.payment.service.*;
import com.ridelink.support.AccessFailure;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest(properties={"spring.datasource.url=jdbc:h2:mem:paymenttests;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa","spring.datasource.password=","spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"})
class PaymentPersistenceTest {
    @Autowired FinalFareService finalizer;
    @Autowired PaymentService service;
    @Autowired FinalFareRepository fares;
    @Autowired PaymentRepository payments;
    @Autowired ReceiptRepository receipts;
    UUID ride;
    @BeforeEach void setup() {
        receipts.deleteAll();payments.deleteAll();fares.deleteAll();ride=UUID.randomUUID();
        finalizer.ensure(new Trip(ride,"passenger-account","COMPLETED",new BigDecimal("8"),20L));
    }
    ProcessPaymentRequest request(String key,boolean fail) {return new ProcessPaymentRequest(ride,key,Method.CARD,fail);}
    @Test void successCreatesImmutableBreakdownAndRetryReturnsSameAttempt() {
        var paid=service.process(request("one",false));
        assertThat(paid.amount).isEqualByComparingTo("1150.00");
        assertThat(service.process(request("one",false)).id).isEqualTo(paid.id);
        assertThat(receipts.findById(paid.receiptId).orElseThrow().total).isEqualByComparingTo("1150.00");
        assertThat(payments.count()).isEqualTo(1);assertThat(receipts.count()).isEqualTo(1);
        assertThatThrownBy(()->service.process(request("another",false))).isInstanceOf(AccessFailure.class);
    }
    @Test void failureHasNoReceiptAndNewAttemptCanSucceed() {
        var failed=service.process(request("failed",true));
        assertThat(failed.status).isEqualTo("FAILED");assertThat(failed.receiptId).isNull();assertThat(failed.paidAt).isNull();
        assertThat(service.process(request("retry",false)).status).isEqualTo("SUCCESS");
        assertThat(payments.count()).isEqualTo(2);assertThat(receipts.count()).isEqualTo(1);
    }
    @Test void keyCannotChangeMeaning() {
        service.process(request("fixed",true));
        assertThatThrownBy(()->service.process(request("fixed",false))).isInstanceOf(AccessFailure.class);
    }
    @Test void finalFareRequiresCompletionAndRemainsStable() {
        assertThatThrownBy(()->finalizer.ensure(new Trip(UUID.randomUUID(),"p","IN_PROGRESS",BigDecimal.ONE,1L)))
                .isInstanceOf(AccessFailure.class);
        var same=finalizer.ensure(new Trip(ride,"passenger-account","COMPLETED",new BigDecimal("99"),99L));
        assertThat(same.total).isEqualByComparingTo("1150.00");
    }
    @Test void concurrentDifferentKeysAllowOnlyOneSuccessfulPayment() throws Exception {
        var pool=Executors.newFixedThreadPool(4);var start=new CountDownLatch(1);
        try {
            var results=new ArrayList<Future<Boolean>>();
            for(int i=0;i<4;i++) {String key="race-"+i;results.add(pool.submit(()->{
                start.await();try{service.process(request(key,false));return true;}
                catch(AccessFailure ex){assertThat(ex.status()).isEqualTo(409);return false;}
            }));}
            start.countDown();int winners=0;for(var result:results) if(result.get(10,TimeUnit.SECONDS)) winners++;
            assertThat(winners).isEqualTo(1);assertThat(receipts.count()).isEqualTo(1);
        } finally {pool.shutdownNow();}
    }
    @Test void concurrentSameKeyReturnsOnePayment() throws Exception {
        var pool=Executors.newFixedThreadPool(3);
        try {
            var results=new ArrayList<Future<UUID>>();
            for(int i=0;i<3;i++) results.add(pool.submit(()->service.process(request("same",false)).id));
            var ids=new HashSet<UUID>();for(var result:results) ids.add(result.get(10,TimeUnit.SECONDS));
            assertThat(ids).hasSize(1);assertThat(payments.count()).isEqualTo(1);
        } finally {pool.shutdownNow();}
    }
}
