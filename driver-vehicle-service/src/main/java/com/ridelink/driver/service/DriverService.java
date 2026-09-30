package com.ridelink.driver.service;

import com.ridelink.driver.document.Driver;
import com.ridelink.driver.domain.AvailabilityStatus;
import com.ridelink.driver.dto.request.CreateDriverRequest;
import com.ridelink.driver.dto.response.DriverResponse;
import com.ridelink.driver.exception.DriverNotFoundException;
import com.ridelink.driver.repository.DriverRepository;
import java.util.Locale;
import org.springframework.stereotype.Service;

@Service
public class DriverService {
    private final DriverRepository driverRepository;

    public DriverService(DriverRepository driverRepository) {
        this.driverRepository = driverRepository;
    }

    public DriverResponse create(CreateDriverRequest request) {
        Driver driver = new Driver(null, request.accountId().strip(),
                request.licenseNumber().strip().toUpperCase(Locale.ROOT),
                request.serviceArea().strip(), AvailabilityStatus.OFFLINE);
        return DriverResponse.from(driverRepository.insert(driver));
    }

    public DriverResponse getById(String driverId) {
        return driverRepository.findById(driverId)
                .map(DriverResponse::from)
                .orElseThrow(DriverNotFoundException::new);
    }
}
