package com.uber.paymentservice.exception;

public class PaymentVerificationException extends PaymentServiceException {
    public PaymentVerificationException(String message) {
        super(message);
    }
    public PaymentVerificationException(String message, Throwable cause) {
        super(message, cause);
    }
}