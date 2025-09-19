package com.uber.paymentservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @AllArgsConstructor
public class InitiatePaymentResponse {
    private Long paymentId;
    private String rideId;
    private Double amount;
    private String currency;
    private String status; // PENDING
}