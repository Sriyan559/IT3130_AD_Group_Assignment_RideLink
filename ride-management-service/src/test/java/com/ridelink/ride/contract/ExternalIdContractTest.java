package com.ridelink.ride.contract;

import com.ridelink.ride.controller.RideController;
import com.ridelink.ride.entity.Ride;
import com.ridelink.ride.exception.ApiExceptionHandler;
import com.ridelink.ride.lifecycle.RideLifecycle;
import com.ridelink.ride.mapper.RideMapper;
import com.ridelink.ride.repository.RideRepository;
import com.ridelink.ride.service.RideService;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static java.util.Objects.requireNonNull;

class ExternalIdContractTest {
    private static final String PASSENGER = "6abbfa6d095b5451ceb2f209";
    private static final String DRIVER = "6abbfa6d095b5451ceb2f20a";
    private final RideRepository repository = mock(RideRepository.class);
    private MockMvc mvc;
    private Ride saved;

    @BeforeEach
    // Mockito matchers/captors return null placeholders; the mock does not consume them.
    @SuppressWarnings("null")
    void setup() {
        var service = new RideService(repository, new RideLifecycle(), new RideMapper());
        service.setDriverGateway(mock(com.ridelink.ride.integration.DriverGateway.class));
        when(repository.findById(any())).thenAnswer(call -> Optional.ofNullable(saved));
        mvc = MockMvcBuilders.standaloneSetup(new RideController(service))
                .setControllerAdvice(new ApiExceptionHandler()).build();
        when(repository.save(any(Ride.class))).thenAnswer(call -> {
            saved = call.getArgument(0);
            return saved;
        });
    }

    private String body(String passengerJson, String driverJson) {
        return """
                {"passengerId":%s,"driverId":%s,"pickupLatitude":6.9271,"pickupLongitude":79.8612,
                 "pickupAddress":"Colombo","destinationLatitude":6.9147,"destinationLongitude":79.8732,
                 "destinationAddress":"Destination"}
                """.formatted(passengerJson, driverJson);
    }

    @Test
    void createAndReadPreserveExternalStringIdsAndRideUuid() throws Exception {
        mvc.perform(post("/api/v1/rides").contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requireNonNull(body("\"" + PASSENGER + "\"", "\"" + DRIVER + "\""))))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.passengerId").value(PASSENGER))
                .andExpect(jsonPath("$.driverId").value(DRIVER)).andExpect(jsonPath("$.status").value("ASSIGNED"));
        assertThat(saved.getPassengerId()).isEqualTo(PASSENGER);
        assertThat(saved.getDriverId()).isEqualTo(DRIVER);
        assertThat(saved.getId()).isInstanceOf(UUID.class);
        when(repository.findById(requireNonNull(saved.getId()))).thenReturn(Optional.of(saved));
        mvc.perform(get("/api/v1/rides/" + saved.getId())).andExpect(status().isOk())
                .andExpect(jsonPath("$.passengerId").value(PASSENGER)).andExpect(jsonPath("$.driverId").value(DRIVER));
    }

    @Test
    void unassignedRideAcceptsStringDriverInAssignmentPath() throws Exception {
        mvc.perform(post("/api/v1/rides").contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requireNonNull(body("\"" + PASSENGER + "\"", "null"))))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.status").value("REQUESTED"));
        UUID id = saved.getId();
        when(repository.findById(requireNonNull(id))).thenReturn(Optional.of(saved));
        mvc.perform(patch("/api/v1/rides/" + id + "/assign-driver/" + DRIVER))
                .andExpect(status().isOk()).andExpect(jsonPath("$.driverId").value(DRIVER))
                .andExpect(jsonPath("$.status").value("ASSIGNED"));
        assertThat(saved.getDriverId()).isEqualTo(DRIVER);
    }

    @Test
    void historyPathsForwardExactExternalIdsToRepository() throws Exception {
        Ride ride = new Ride();
        ride.setPassengerId(PASSENGER);
        ride.setDriverId(DRIVER);
        ride.prepareForSave();
        when(repository.findByPassengerIdOrderByCreatedAtDesc(PASSENGER)).thenReturn(List.of(ride));
        when(repository.findByDriverIdOrderByCreatedAtDesc(DRIVER)).thenReturn(List.of(ride));
        mvc.perform(get("/api/v1/rides/passenger/" + PASSENGER)).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].passengerId").value(PASSENGER));
        mvc.perform(get("/api/v1/rides/driver/" + DRIVER)).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].driverId").value(DRIVER));
        verify(repository).findByPassengerIdOrderByCreatedAtDesc(PASSENGER);
        verify(repository).findByDriverIdOrderByCreatedAtDesc(DRIVER);
    }

    @ParameterizedTest
    @ValueSource(strings = {"null", "\"\"", "\"   \""})
    // Mockito matchers/captors return null placeholders; the mock does not consume them.
    @SuppressWarnings("null")
    void missingOrBlankPassengerIsRejected(String passenger) throws Exception {
        mvc.perform(post("/api/v1/rides").contentType(MediaType.APPLICATION_JSON_VALUE).content(requireNonNull(body(passenger, "null"))))
                .andExpect(status().isBadRequest());
        verify(repository, never()).save(any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"\"\"", "\"   \""})
    // Mockito matchers/captors return null placeholders; the mock does not consume them.
    @SuppressWarnings("null")
    void blankOptionalDriverIsRejected(String driver) throws Exception {
        mvc.perform(post("/api/v1/rides").contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requireNonNull(body("\"" + PASSENGER + "\"", driver))))
                .andExpect(status().isBadRequest());
        verify(repository, never()).save(any());
    }
}
