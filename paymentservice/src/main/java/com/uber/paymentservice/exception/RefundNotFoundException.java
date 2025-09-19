package com.uber.paymentservice.exception;

public class RefundNotFoundException extends NotFoundException {
    public RefundNotFoundException(String identifier) {
        super("Refund with identifier '" + identifier + "' not found.");
    }
}