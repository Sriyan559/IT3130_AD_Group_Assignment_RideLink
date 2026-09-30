package com.ridelink.driver.unit;

import com.ridelink.driver.document.Driver;
import com.ridelink.driver.domain.AvailabilityStatus;
import com.ridelink.driver.dto.request.CreateDriverRequest;
import com.ridelink.driver.exception.DriverNotFoundException;
import java.util.Optional;
import com.ridelink.driver.repository.DriverRepository;
import com.ridelink.driver.service.DriverService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DriverServiceTest {
    @Test
    void retrievesExistingProfileWithoutWritingToDatabase() {
        DriverRepository repository = mock(DriverRepository.class);
        when(repository.findById("driver-1")).thenReturn(Optional.of(new Driver(
                "driver-1", "account-1", "B1234567", "Malabe", AvailabilityStatus.OFFLINE)));

        var response = new DriverService(repository).getById("driver-1");

        assertThat(response.id()).isEqualTo("driver-1");
        assertThat(response.accountId()).isEqualTo("account-1");
        assertThat(response.licenseNumber()).isEqualTo("B1234567");
        assertThat(response.serviceArea()).isEqualTo("Malabe");
        assertThat(response.availabilityStatus()).isEqualTo(AvailabilityStatus.OFFLINE);
        verify(repository).findById("driver-1");
        verifyNoMoreInteractions(repository);
    }

    @Test
    void missingDriverThrowsNotFoundWithoutCreatingProfile() {
        DriverRepository repository = mock(DriverRepository.class);
        when(repository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> new DriverService(repository).getById("missing"))
                .isInstanceOf(DriverNotFoundException.class);
        verify(repository).findById("missing");
        verifyNoMoreInteractions(repository);
    }

    @Test
    void createsOfflineDriverWithNormalizedValuesAndDatabaseId() {
        DriverRepository repository = mock(DriverRepository.class);
        when(repository.insert(any(Driver.class))).thenAnswer(invocation -> {
            Driver driver = invocation.getArgument(0);
            return new Driver("generated-id", driver.accountId(), driver.licenseNumber(),
                    driver.serviceArea(), driver.availabilityStatus());
        });

        var response = new DriverService(repository).create(
                new CreateDriverRequest(" account-1 ", " b1234567 ", " Malabe "));

        ArgumentCaptor<Driver> saved = ArgumentCaptor.forClass(Driver.class);
        verify(repository).insert(saved.capture());
        assertThat(saved.getValue().id()).isNull();
        assertThat(response.id()).isEqualTo("generated-id");
        assertThat(response.accountId()).isEqualTo("account-1");
        assertThat(response.licenseNumber()).isEqualTo("B1234567");
        assertThat(response.serviceArea()).isEqualTo("Malabe");
        assertThat(response.availabilityStatus()).isEqualTo(AvailabilityStatus.OFFLINE);
    }
}
