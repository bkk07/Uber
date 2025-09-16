package com.uber.addressservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidAddressException extends RuntimeException {
    public InvalidAddressException(String message) {
        super(message);
    }
}