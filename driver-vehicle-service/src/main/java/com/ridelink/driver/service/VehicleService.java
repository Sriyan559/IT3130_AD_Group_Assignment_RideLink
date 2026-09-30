package com.ridelink.driver.service;

import com.ridelink.driver.document.Vehicle;
import com.ridelink.driver.dto.request.RegisterVehicleRequest;
import com.ridelink.driver.dto.response.VehicleResponse;
import com.ridelink.driver.exception.DriverNotFoundException;
import com.ridelink.driver.exception.VehicleAlreadyExistsException;
import com.ridelink.driver.repository.DriverRepository;
import com.ridelink.driver.repository.VehicleRepository;
import java.util.Locale;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

@Service
public class VehicleService {
    private final DriverRepository drivers;
    private final VehicleRepository vehicles;

    public VehicleService(DriverRepository drivers, VehicleRepository vehicles) {
        this.drivers = drivers;
        this.vehicles = vehicles;
    }

    public VehicleResponse register(String driverId, RegisterVehicleRequest request) {
        if (!drivers.existsById(driverId)) {
            throw new DriverNotFoundException();
        }
        String plate = request.plateNumber().replace(" ", "").replace("-", "")
                .toUpperCase(Locale.ROOT);
        Vehicle vehicle = new Vehicle(null, driverId, plate, request.make().strip(),
                request.model().strip(), request.vehicleClass().strip().toUpperCase(Locale.ROOT),
                request.capacity());
        try {
            return VehicleResponse.from(vehicles.insert(vehicle));
        } catch (DuplicateKeyException exception) {
            throw new VehicleAlreadyExistsException(exception);
        }
    }
}
