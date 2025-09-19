package com.uber.paymentservice.exception;

public class BusinessLogicException extends PaymentServiceException {
    public BusinessLogicException(String message) {
        super(message);
    }
    public BusinessLogicException(String message, Throwable cause) {
        super(message, cause);
    }
}