package com.uber.walletservice.dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DeductMoneyRequestDTO {
    @NotNull(message = "User ID cannot be null")
    private Long userId;
    @NotNull(message = "Amount cannot be null")
    @DecimalMin(value = "0.01", inclusive = true, message = "Amount must be positive")
    private BigDecimal amount;
    @NotNull(message = "Reference ID for the deduction (e.g., ride ID) cannot be null")
    private Long rideId;

    private Long promoAppliedId;
}