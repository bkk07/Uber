package com.uber.paymentservice.exception;

public class NotFoundException extends PaymentServiceException {
    public NotFoundException(String message) {
        super(message);
    }
}