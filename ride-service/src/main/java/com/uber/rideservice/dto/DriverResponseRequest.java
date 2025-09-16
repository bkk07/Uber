package com.uber.rideservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DriverResponseRequest {
    @NotNull(message = "Driver ID cannot be null")
    private Long driverId;
    @NotNull(message = "Accepted status cannot be null")
    private Boolean accepted;
}