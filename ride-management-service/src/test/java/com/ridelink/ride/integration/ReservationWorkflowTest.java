package com.ridelink.ride.integration;

import com.ridelink.ride.domain.RideStatus;
import com.ridelink.ride.entity.Ride;
import com.ridelink.ride.repository.RideRepository;
import com.ridelink.ride.service.RideService;
import com.ridelink.ride.lifecycle.RideLifecycle;
import com.ridelink.ride.mapper.RideMapper;
import com.ridelink.ride.dto.request.UpdateRideStatusRequest;
import com.ridelink.support.AccessFailure;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;
import static org.assertj.core.api.Assertions.*;
import static java.util.Objects.requireNonNull;

class ReservationWorkflowTest {
    RideRepository repo = mock(RideRepository.class);
    DriverGateway gateway = mock(DriverGateway.class);
    PaymentGateway payment = mock(PaymentGateway.class);
    RideService service = new RideService(repo, new RideLifecycle(), new RideMapper());
    Ride ride = new Ride();
    // Mockito returns null placeholders when registering matchers.
    @SuppressWarnings("null")
    @BeforeEach void setup() {
        service.setDriverGateway(gateway);
        when(payment.process(any(),anyBoolean())).thenReturn(new PaymentGateway.Result(UUID.randomUUID(),UUID.randomUUID(),"SUCCESS"));
        service.setPaymentGateway(payment);
        ride.setPassengerId("account-p"); ride.prepareForSave();
        when(repo.findById(requireNonNull(ride.getId()))).thenReturn(Optional.of(ride));
        when(repo.save(any())).thenAnswer(call -> call.getArgument(0));
    }
    @Test void reservesBeforeAssignedAndSameAssignmentIsIdempotent() {
        assertThat(service.assignDriver(ride.getId(), "driver-a").status()).isEqualTo(RideStatus.ASSIGNED);
        service.assignDriver(ride.getId(), "driver-a");
        verify(gateway, times(1)).reserve("driver-a", ride.getId());
        assertThat(ride.getPendingDriverId()).isNull();
        assertThatThrownBy(() -> service.assignDriver(ride.getId(), "driver-b")).isInstanceOf(AccessFailure.class);
    }
    @Test void ambiguousReservationFailureRetainsIntentAndRetryUsesSameRide() {
        doThrow(new AccessFailure(503,"timeout")).doNothing().when(gateway).reserve("driver-a",ride.getId());
        assertThatThrownBy(() -> service.assignDriver(ride.getId(),"driver-a")).isInstanceOf(AccessFailure.class);
        assertThat(ride.getStatus()).isEqualTo(RideStatus.REQUESTED);
        assertThat(ride.getPendingDriverId()).isEqualTo("driver-a");
        assertThatThrownBy(() -> service.cancel(ride.getId())).isInstanceOf(AccessFailure.class);
        service.assignDriver(ride.getId(),"driver-a");
        assertThat(ride.getStatus()).isEqualTo(RideStatus.ASSIGNED);
    }
    @Test void definiteConflictClearsPendingIntent() {
        doThrow(new AccessFailure(409,"busy")).when(gateway).reserve("driver-a",ride.getId());
        assertThatThrownBy(() -> service.assignDriver(ride.getId(),"driver-a")).isInstanceOf(AccessFailure.class);
        assertThat(ride.getPendingDriverId()).isNull();
        assertThat(ride.getDriverId()).isNull();
    }
    @Test void completionPersistsReleaseIntentAndRecoversAfterFailure() {
        ride.setDriverId("driver-a"); ride.setStatus(RideStatus.IN_PROGRESS);
        doThrow(new AccessFailure(503,"timeout")).doNothing().when(gateway).release("driver-a",ride.getId());
        var completed = new UpdateRideStatusRequest(RideStatus.COMPLETED, java.math.BigDecimal.ONE, 1L);
        assertThatThrownBy(() -> service.updateStatus(ride.getId(),completed)).isInstanceOf(AccessFailure.class);
        assertThat(ride.getStatus()).isEqualTo(RideStatus.COMPLETED);
        assertThat(ride.isReleasePending()).isTrue();
        service.updateStatus(ride.getId(),completed);
        assertThat(ride.isReleasePending()).isFalse();
    }
    @Test void cancellationReleasesOnlyAssignedDriver() {
        service.cancel(ride.getId()); verifyNoInteractions(gateway);
        ride.setStatus(RideStatus.ASSIGNED); ride.setDriverId("driver-a");
        service.cancel(ride.getId()); service.cancel(ride.getId());
        verify(gateway, times(1)).release("driver-a",ride.getId());
    }
    @Test void statusCannotBypassReservation() {
        assertThatThrownBy(() -> service.updateStatus(ride.getId(),
                new UpdateRideStatusRequest(RideStatus.ASSIGNED,null,null))).isInstanceOf(AccessFailure.class);
        verifyNoInteractions(gateway);
    }
    @Test void completionRequiresTrustedTripMetrics() {
        ride.setDriverId("driver-a");ride.setStatus(RideStatus.IN_PROGRESS);
        assertThatThrownBy(()->service.updateStatus(ride.getId(),new UpdateRideStatusRequest(RideStatus.COMPLETED,null,null)))
                .isInstanceOf(AccessFailure.class);
        assertThat(ride.getStatus()).isEqualTo(RideStatus.IN_PROGRESS);
        verifyNoInteractions(payment);
    }
    @Test void paymentFailureRetainsDurableIntentAndRetriesSameRide() {
        ride.setDriverId("driver-a");ride.setStatus(RideStatus.IN_PROGRESS);
        when(payment.process(ride.getId(),false)).thenThrow(new AccessFailure(503,"offline"))
                .thenReturn(new PaymentGateway.Result(UUID.randomUUID(),UUID.randomUUID(),"SUCCESS"));
        var completed=new UpdateRideStatusRequest(RideStatus.COMPLETED,java.math.BigDecimal.ONE,1L);
        assertThatThrownBy(()->service.updateStatus(ride.getId(),completed)).isInstanceOf(AccessFailure.class);
        assertThat(ride.isPaymentPending()).isTrue();assertThat(ride.isReleasePending()).isFalse();
        service.updateStatus(ride.getId(),completed);
        assertThat(ride.isPaymentPending()).isFalse();assertThat(ride.getPaymentStatus()).isEqualTo("SUCCESS");
        verify(payment,times(2)).process(ride.getId(),false);
    }
    @Test void matchingReturnsNoAvailableDriverWithoutAssignment() {
        ride.setPickupLatitude(java.math.BigDecimal.ZERO);ride.setPickupLongitude(java.math.BigDecimal.ZERO);
        when(gateway.eligible(any(),any(),any())).thenReturn(List.of());
        assertThatThrownBy(()->service.matchDriver(ride.getId(),java.math.BigDecimal.ONE))
                .isInstanceOf(AccessFailure.class).hasMessage("NO_AVAILABLE_DRIVER");
        assertThat(ride.getStatus()).isEqualTo(RideStatus.REQUESTED);
    }
    @Test void matchingTriesNextCandidateAfterReservationConflict() {
        ride.setPickupLatitude(java.math.BigDecimal.ZERO);ride.setPickupLongitude(java.math.BigDecimal.ZERO);
        when(gateway.eligible(any(),any(),any())).thenReturn(List.of(new DriverGateway.Candidate("busy"),new DriverGateway.Candidate("free")));
        doThrow(new AccessFailure(409,"busy")).when(gateway).reserve("busy",ride.getId());
        assertThat(service.matchDriver(ride.getId(),java.math.BigDecimal.ONE).driverId()).isEqualTo("free");
    }
}
