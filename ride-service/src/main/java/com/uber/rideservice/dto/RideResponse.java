package com.uber.rideservice.dto;

import com.uber.rideservice.entity.RideStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class RideResponse {
    private Long id;
    private Long userId;
    private Long driverId;
    private String pickupLocation;
    private String dropLocation;
    private Double fareEstimate;
    private RideStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<DriverDto> nearbyDrivers; // To return with initial ride request
    private String message; // For general response messages
}