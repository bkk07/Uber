package com.uber.rideservice.controller;
import com.uber.rideservice.dto.DriverResponseRequest;
import com.uber.rideservice.dto.InitializeRideRequest;
import com.uber.rideservice.dto.RideResponse;
import com.uber.rideservice.dto.SelectDriverRequest;
import com.uber.rideservice.service.RideService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rides")
public class RideController {
    private final RideService rideService;
    public RideController(RideService rideService) {
        this.rideService = rideService;
    }
    @PostMapping("/init")
    public ResponseEntity<RideResponse> initializeRide(@Valid @RequestBody InitializeRideRequest request) {
        RideResponse response = rideService.initializeRide(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    @PostMapping("/{rideId}/select-driver")
    public ResponseEntity<RideResponse> selectDriver(@PathVariable Long rideId, @Valid @RequestBody SelectDriverRequest request) {
        RideResponse response = rideService.selectDriver(rideId, request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    @PostMapping("/{rideId}/driver-response")
    public ResponseEntity<RideResponse> handleDriverResponse(@PathVariable Long rideId, @Valid @RequestBody DriverResponseRequest request) {
        RideResponse response = rideService.handleDriverResponse(rideId, request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/{rideId}/start")
    public ResponseEntity<RideResponse> startRide(@PathVariable Long rideId) {
        RideResponse response = rideService.startRide(rideId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/{rideId}/complete")
    public ResponseEntity<RideResponse> completeRide(@PathVariable Long rideId) {
        RideResponse response = rideService.completeRide(rideId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/{rideId}/cancel")
    public ResponseEntity<RideResponse> cancelRide(@PathVariable Long rideId) {
        RideResponse response = rideService.cancelRide(rideId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // Global exception handler for RideNotFoundException (or other custom exceptions)
    @ExceptionHandler(value = {com.uber.rideservice.exception.RideNotFoundException.class, IllegalStateException.class})
    public ResponseEntity<String> handleNotFoundException(RuntimeException ex) {
        if (ex instanceof com.uber.rideservice.exception.RideNotFoundException) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<RideResponse>> getRideByUserId(@PathVariable Long userId) {
        List<RideResponse> response = rideService.getRideByUserId(userId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    @GetMapping("/driver/{driverId}")
    public ResponseEntity<List<RideResponse>> getRideByDriverId(@PathVariable Long driverId) {
        List<RideResponse> response = rideService.getRideByDriverId(driverId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}