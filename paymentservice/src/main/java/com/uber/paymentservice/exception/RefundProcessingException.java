package com.uber.paymentservice.exception;

public class RefundProcessingException extends PaymentServiceException {
    public RefundProcessingException(String message) {
        super(message);
    }
    public RefundProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}