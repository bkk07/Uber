package com.uber.paymentservice.exception;

// Base custom exception for the Payment Service
public class PaymentServiceException extends RuntimeException {
    public PaymentServiceException(String message) {
        super(message);
    }
    public PaymentServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}