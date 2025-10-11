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
    // Latitude and longitude for the pickup location
    @NotNull(message = "Pickup latitude cannot be null")
    private Double pickupLocationLat;

    @NotNull(message = "Pickup longitude cannot be null")
    private Double pickupLocationLon;

    // Latitude and longitude for the drop location
    @NotNull(message = "Drop latitude cannot be null")
    private Double dropLocationLat;

    @NotNull(message = "Drop longitude cannot be null")
    private Double dropLocationLon;

}