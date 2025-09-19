package com.uber.paymentservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class VerifyPaymentRequest {
    @NotBlank(message = "Razorpay Order ID cannot be blank")
    @Size(max = 100, message = "Razorpay Order ID cannot exceed 100 characters")
    private String razorpay_order_id;

    @NotBlank(message = "Razorpay Payment ID cannot be blank")
    @Size(max = 100, message = "Razorpay Payment ID cannot exceed 100 characters")
    private String razorpay_payment_id;

    @NotBlank(message = "Razorpay Signature cannot be blank")
    private String razorpay_signature;
}