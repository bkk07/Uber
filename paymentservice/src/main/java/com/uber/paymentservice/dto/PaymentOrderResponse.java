package com.uber.paymentservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @AllArgsConstructor
public class PaymentOrderResponse {
    private String orderId;    // Razorpay order_id
    private Double amount;     // Amount in smallest unit (e.g., paise for INR)
    private String currency;
    private String key;        // Razorpay Key ID
}