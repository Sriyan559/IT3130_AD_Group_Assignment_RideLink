package com.ridelink.driver.unit;

import com.ridelink.driver.document.Vehicle;
import com.ridelink.driver.dto.request.RegisterVehicleRequest;
import com.ridelink.driver.exception.DriverNotFoundException;
import com.ridelink.driver.exception.VehicleAlreadyExistsException;
import com.ridelink.driver.repository.DriverRepository;
import com.ridelink.driver.repository.VehicleRepository;
import com.ridelink.driver.service.VehicleService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DuplicateKeyException;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class VehicleServiceTest {
    private final DriverRepository drivers = mock(DriverRepository.class);
    private final VehicleRepository vehicles = mock(VehicleRepository.class);
    private final VehicleService service = new VehicleService(drivers, vehicles);

    private RegisterVehicleRequest request(String plate) {
        return new RegisterVehicleRequest(plate, " Toyota ", " Prius ", " car ", 4);
    }

    @ParameterizedTest
    @ValueSource(strings = {"abc-1234", " ABC1234 ", "abc 1234"})
    void normalizesPlateAndAttributesWithoutChangingDriver(String plate) {
        when(drivers.existsById("driver-1")).thenReturn(true);
        when(vehicles.insert(any(Vehicle.class))).thenAnswer(invocation -> {
            Vehicle vehicle = invocation.getArgument(0);
            return new Vehicle("vehicle-1", vehicle.driverId(), vehicle.plateNumber(),
                    vehicle.make(), vehicle.model(), vehicle.vehicleClass(), vehicle.capacity());
        });

        var result = service.register("driver-1", request(plate));

        assertThat(result.id()).isEqualTo("vehicle-1");
        assertThat(result.driverId()).isEqualTo("driver-1");
        assertThat(result.plateNumber()).isEqualTo("ABC1234");
        assertThat(result.make()).isEqualTo("Toyota");
        assertThat(result.model()).isEqualTo("Prius");
        assertThat(result.vehicleClass()).isEqualTo("CAR");
        assertThat(result.capacity()).isEqualTo(4);
        verify(vehicles).insert(new Vehicle(null, "driver-1", "ABC1234", "Toyota", "Prius", "CAR", 4));
        verify(drivers).existsById("driver-1");
        verifyNoMoreInteractions(drivers);
    }

    @Test
    void missingDriverDoesNotInsertVehicle() {
        assertThatThrownBy(() -> service.register("missing", request("ABC1234")))
                .isInstanceOf(DriverNotFoundException.class);
        verifyNoInteractions(vehicles);
    }

    @Test
    void duplicateDatabaseConstraintBecomesVehicleConflict() {
        when(drivers.existsById("driver-1")).thenReturn(true);
        when(vehicles.insert(any(Vehicle.class))).thenThrow(new DuplicateKeyException("internal details"));
        assertThatThrownBy(() -> service.register("driver-1", request("abc-1234")))
                .isInstanceOf(VehicleAlreadyExistsException.class)
                .hasMessage("A vehicle with this plate number already exists");
    }

    @Test
    void databaseFailureIsNotMistakenForDuplicate() {
        when(drivers.existsById("driver-1")).thenReturn(true);
        when(vehicles.insert(any(Vehicle.class)))
                .thenThrow(new DataAccessResourceFailureException("offline"));
        assertThatThrownBy(() -> service.register("driver-1", request("ABC1234")))
                .isInstanceOf(DataAccessResourceFailureException.class);
    }

    @Test
    void driverLookupFailureDoesNotInsertVehicle() {
        when(drivers.existsById("driver-1"))
                .thenThrow(new DataAccessResourceFailureException("offline"));
        assertThatThrownBy(() -> service.register("driver-1", request("ABC1234")))
                .isInstanceOf(DataAccessResourceFailureException.class);
        verifyNoInteractions(vehicles);
    }
}
