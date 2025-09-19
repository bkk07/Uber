package com.uber.paymentservice.exception;

public class InvalidPaymentStateException extends PaymentServiceException {
    public InvalidPaymentStateException(String message) {
        super(message);
    }
    public InvalidPaymentStateException(String paymentId, String currentState, String expectedState) {
        super(String.format("Payment %s is in state '%s'. Expected state: '%s'.", paymentId, currentState, expectedState));
    }
    public InvalidPaymentStateException(Long paymentId, String currentState, String expectedState) {
        super(String.format("Payment %d is in state '%s'. Expected state: '%s'.", paymentId, currentState, expectedState));
    }
}