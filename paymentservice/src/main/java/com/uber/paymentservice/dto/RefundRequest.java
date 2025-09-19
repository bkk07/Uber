package com.uber.paymentservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class RefundRequest {
    @NotNull(message = "Payment ID cannot be null")
    @Min(value = 1, message = "Payment ID must be a positive number")
    private Long paymentId;

    @NotNull(message = "Amount cannot be null")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0 for refund")
    private Double amount;
}