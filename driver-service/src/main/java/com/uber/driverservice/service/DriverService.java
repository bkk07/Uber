package com.uber.driverservice.service;

import com.uber.driverservice.enums.DriverStatus;
import com.uber.driverservice.dto.DriverAuthResponse;
import com.uber.driverservice.dto.DriverRequest;
import com.uber.driverservice.dto.DriverResponse;

import java.util.List;
import java.util.Optional;

public interface DriverService {
    DriverResponse createDriver(DriverRequest request);
    DriverResponse getDriverById(Long id);
    List<DriverResponse> getAllDrivers();
    DriverResponse updateDriver(Long id, DriverRequest request);
    void deleteDriver(Long id);
    DriverResponse updateStatus(Long id, DriverStatus status);
    DriverResponse updateLocation(Long id, double latitude, double longitude);
    List<DriverResponse> getAllAvailableDrivers(); // Exposing the custom repository method
    Optional<DriverResponse> findNearestDriver(double pickupLat, double pickupLon);
    DriverAuthResponse driverExists(String loginId, String password);
}
