package com.uber.paymentservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @AllArgsConstructor
public class RefundResponse {
    private String refundId;
    private String status; // SUCCESS, FAILED
}