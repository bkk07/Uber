package com.uber.paymentservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class InitiatePaymentRequest {
    @NotBlank(message = "Ride ID cannot be blank")
    @Size(max = 50, message = "Ride ID cannot exceed 50 characters")
    private String rideId;

    @NotNull(message = "User ID cannot be null")
    @Min(value = 1, message = "User ID must be a positive number")
    private Long userId;

    @NotNull(message = "Driver ID cannot be null")
    @Min(value = 1, message = "Driver ID must be a positive number")
    private Long driverId;

    @NotNull(message = "Amount cannot be null")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private Double amount;

    @NotBlank(message = "Currency cannot be blank")
    @Size(min = 3, max = 3, message = "Currency must be a 3-letter ISO code (e.g., INR)")
    private String currency;
}