package com.uber.driverservice.service;
import com.uber.driverservice.dto.DriverAuthResponse;
import com.uber.driverservice.dto.DriverDto;
import com.uber.driverservice.dto.DriverRequest;
import com.uber.driverservice.dto.DriverResponse;
import com.uber.driverservice.enums.DriverRole;
import com.uber.driverservice.enums.DriverStatus;
import com.uber.driverservice.exception.DuplicateUserException;
import com.uber.driverservice.exception.EmailAlreadyExistsException;
import com.uber.driverservice.exception.DriverNotFoundException;
import com.uber.driverservice.model.Driver;
import com.uber.driverservice.repository.DriverRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor
public class DriverServiceImpl implements DriverService {
    private final DriverRepository driverRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    @Override
    public DriverResponse createDriver(DriverRequest request) {

        if (driverRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateUserException("Username '" + request.getUsername() + "' already exists.");
        }
        if (driverRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Driver with email " + request.getEmail() + " already exists.");
        }

        Driver driver = Driver.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .phone(request.getPhone())
                .status(request.getStatus())
                .password(passwordEncoder.encode(request.getPassword()))
                .driverRole(DriverRole.DRIVER)
                .latitude(request.getLatitude() != null ? request.getLatitude() : 0.0) // Default if not provided
                .longitude(request.getLongitude() != null ? request.getLongitude() : 0.0) // Default if not provided
                .vehicle(request.getVehicle())
                .perKilometer(request.getPerKilometer())
                .build();

        Driver savedDriver = driverRepository.save(driver);
        return mapToDriverResponse(savedDriver);
    }

    @Override
    public DriverResponse getDriverById(Long id) {
        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new DriverNotFoundException("Driver with id " + id + " not found."));
        return mapToDriverResponse(driver);
    }

    @Override
    public List<DriverResponse> getAllDrivers() {
        List<Driver> drivers = driverRepository.findAll();
        return drivers.stream()
                .map(this::mapToDriverResponse)
                .collect(Collectors.toList());
    }

    @Override
    public DriverResponse updateDriver(Long id, DriverRequest request) {
        Driver existingDriver = driverRepository.findById(id)
                .orElseThrow(() -> new DriverNotFoundException("Driver with id " + id + " not found."));

        // Check if email is being changed and if new email already exists for another driver
        if (!existingDriver.getEmail().equals(request.getEmail()) && driverRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Driver with email " + request.getEmail() + " already exists.");
        }

        existingDriver.setUsername(request.getUsername());
        existingDriver.setEmail(request.getEmail());
        existingDriver.setPhone(request.getPhone());
        existingDriver.setStatus(request.getStatus());
        existingDriver.setLatitude(request.getLatitude() != null ? request.getLatitude() : existingDriver.getLatitude());
        existingDriver.setLongitude(request.getLongitude() != null ? request.getLongitude() : existingDriver.getLongitude());

        Driver updatedDriver = driverRepository.save(existingDriver);
        return mapToDriverResponse(updatedDriver);
    }

    @Override
    public void deleteDriver(Long id) {
        if (!driverRepository.existsById(id)) {
            throw new DriverNotFoundException("Driver with id " + id + " not found.");
        }
        driverRepository.deleteById(id);
    }
    @Override
    public void updateStatus(Long id, DriverStatus status) {
        Driver existingDriver = driverRepository.findById(id)
                .orElseThrow(() -> new DriverNotFoundException("Driver with id " + id + " not found."));
        existingDriver.setStatus(status);
        Driver updatedDriver = driverRepository.save(existingDriver);
    }
    @Override
    public DriverResponse updateLocation(Long id, double latitude, double longitude) {
        Driver existingDriver = driverRepository.findById(id)
                .orElseThrow(() -> new DriverNotFoundException("Driver with id " + id + " not found."));
        existingDriver.setLatitude(latitude);
        existingDriver.setLongitude(longitude);
        Driver updatedDriver = driverRepository.save(existingDriver);
        return mapToDriverResponse(updatedDriver);
    }

    @Override
    public List<DriverResponse> getAllAvailableDrivers() {
        List<Driver> availableDrivers = driverRepository.findByStatus(DriverStatus.ONLINE);
        return availableDrivers.stream()
                .map(this::mapToDriverResponse)
                .collect(Collectors.toList());
    }


    private DriverResponse mapToDriverResponse(Driver driver) {
        return DriverResponse.builder()
                .id(driver.getId())
                .username(driver.getUsername())
                .email(driver.getEmail())
                .phone(driver.getPhone())
                .status(driver.getStatus())
                .latitude(driver.getLatitude())
                .longitude(driver.getLongitude())
                .createdAt(driver.getCreatedAt())
                .perKilometer(driver.getPerKilometer())
                .vehicle(driver.getVehicle())
                .build();
    }
    @Override
    public List<DriverDto> findNearestDriver(double pickupLat, double pickupLon, int limit) {
        List<Driver> availableDrivers = driverRepository.findByStatus(DriverStatus.ONLINE);
        return availableDrivers.stream()
                .sorted(Comparator.comparingDouble(
                        d -> calculateDistance(pickupLat, pickupLon, d.getLatitude(), d.getLongitude())
                ))
                .limit(limit)
                .map(driver -> DriverDto.builder()
                        .id(driver.getId())
                        .username(driver.getUsername())
                        .phone(driver.getPhone())
                        .latitude(driver.getLatitude())
                        .longitude(driver.getLongitude())
                        .build())
                .toList();
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Earth radius in km
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    @Override
    public DriverAuthResponse driverExists(String loginId, String password) {
        Driver credential = null;
        if (loginId.contains("@")) {
            credential = driverRepository.findByEmail(loginId).orElse(null);
        } else {
            credential = (Driver) driverRepository.findByUsername(loginId).orElse(null);
        }
        if (credential == null) {
            throw new UsernameNotFoundException("Driver not found with loginId: " + loginId);
        }
        // Validate password
        if (!passwordEncoder.matches(password, credential.getPassword())) {
            throw new BadCredentialsException("Invalid password");
        }
        // Return Auth response
        DriverAuthResponse userAuthResponse = new DriverAuthResponse();
        userAuthResponse.setUserId(credential.getId());
        System.out.println(credential.getDriverRole());
        userAuthResponse.setRole(String.valueOf(credential.getDriverRole()));
        return userAuthResponse;
    }

    @Override
    public Boolean isDriverAvailable(Long driverId) {
        Optional<Driver> driver = driverRepository.findById(driverId);
        if(driver.isPresent()) {
            if(driver.get().getStatus() == DriverStatus.ONLINE) {
                return true;
            }
            return false;
        }

        return false;
    }
}