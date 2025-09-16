package com.uber.rideservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SelectDriverRequest {
    @NotNull(message = "Driver ID cannot be null")
    private Long driverId;
}