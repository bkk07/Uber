package com.uber.paymentservice.exception;

public class PaymentNotFoundException extends NotFoundException {
    public PaymentNotFoundException(String identifier) {
        super("Payment with identifier '" + identifier + "' not found.");
    }
}