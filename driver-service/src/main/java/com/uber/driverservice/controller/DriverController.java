package com.uber.driverservice.controller;

import com.uber.driverservice.dto.DriverDto;
import com.uber.driverservice.enums.DriverStatus;
import com.uber.driverservice.dto.DriverAuthResponse;
import com.uber.driverservice.dto.DriverRequest;
import com.uber.driverservice.dto.DriverResponse;
import com.uber.driverservice.exception.DriverNotFoundException;
import com.uber.driverservice.service.DriverService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/drivers")
@RequiredArgsConstructor
public class DriverController {

    private final DriverService driverService;
//
//    @GetMapping("/validate")
//    public DriverAuthResponse driverExists(
//            @RequestParam("loginId") String loginId,
//            @RequestParam("password") String password
//    ){
//        return DriverAuthResponse.builder()
//                .userId(1L)
//                .role("DRIVER")
//                .build();
//    }


    @PostMapping("/register")
    public ResponseEntity<DriverResponse> createDriver(@Valid @RequestBody DriverRequest request) {
        DriverResponse newDriver = driverService.createDriver(request);
        return new ResponseEntity<>(newDriver, HttpStatus.CREATED);
    }
    @PostMapping("/registerAll")
    public ResponseEntity<List<DriverResponse>> createDriver(@Valid @RequestBody List<DriverRequest> request) {
        List<DriverResponse> driverResponses = new ArrayList<>() ;
        for (DriverRequest driverRequest : request) {
            DriverResponse newDriver = driverService.createDriver(driverRequest);
            driverResponses.add(newDriver);
        }
        return new ResponseEntity<>(driverResponses, HttpStatus.CREATED);
    }
    @GetMapping("/{id}")
    public ResponseEntity<DriverResponse> getDriverById(@PathVariable Long id) {
        DriverResponse driver = driverService.getDriverById(id);
        return new ResponseEntity<>(driver, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<DriverResponse>> getAllDrivers() {
        List<DriverResponse> drivers = driverService.getAllDrivers();
        return new ResponseEntity<>(drivers, HttpStatus.OK);
    }
    @GetMapping("/available")
    public ResponseEntity<List<DriverResponse>> getAllAvailableDrivers() {
        List<DriverResponse> availableDrivers = driverService.getAllAvailableDrivers();
        return new ResponseEntity<>(availableDrivers, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DriverResponse> updateDriver(@PathVariable Long id, @Valid @RequestBody DriverRequest request) {
        DriverResponse updatedDriver = driverService.updateDriver(id, request);
        return new ResponseEntity<>(updatedDriver, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDriver(@PathVariable Long id) {
        driverService.deleteDriver(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    @PutMapping("/{id}/status")
    public void updateDriverStatus(
            @PathVariable Long id,
            @RequestParam @NotNull(message = "Availability status cannot be null") DriverStatus status) {
        driverService.updateStatus(id, status);
        return ;
    }

    @PutMapping("/{id}/location")
    public ResponseEntity<DriverResponse> updateLocation(
            @PathVariable Long id,
            @RequestParam @NotNull(message = "Latitude cannot be null") Double latitude,
            @RequestParam @NotNull(message = "Longitude cannot be null") Double longitude) {
        DriverResponse updatedDriver = driverService.updateLocation(id, latitude, longitude);
        return new ResponseEntity<>(updatedDriver, HttpStatus.OK);
    }

    @GetMapping("/api/drivers/nearby")
    List<DriverDto> getNearbyDrivers(
            @RequestParam("latitude") Double latitude,
            @RequestParam("longitude") Double longitude,
            @RequestParam(value = "limit", defaultValue = "5") int limit){
        return driverService.findNearestDriver(latitude,longitude,limit);
    }
    @GetMapping("/validate")
    ResponseEntity<DriverAuthResponse> userExists(
            @RequestParam("loginId") String loginId,
            @RequestParam("password") String password
    ){
        System.out.println("I am in user-service "+loginId+" "+password);
        DriverAuthResponse userAuthResponse =driverService.driverExists(loginId,password);
        System.out.println("This is the response going from driver-service to the auth-service"+userAuthResponse.toString());
        return ResponseEntity.ok(userAuthResponse);
    }
}
