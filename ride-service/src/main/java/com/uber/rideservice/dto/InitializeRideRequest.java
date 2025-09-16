package com.uber.rideservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InitializeRideRequest {
    @NotNull(message = "User ID cannot be null")
    private Long userId;
    @NotNull(message = "Pickup location cannot be null")
    private String pickupLocation;
    @NotNull(message = "Drop location cannot be null")
    private String dropLocation;
}