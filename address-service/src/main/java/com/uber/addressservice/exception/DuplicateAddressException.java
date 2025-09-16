package com.uber.addressservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateAddressException extends RuntimeException {
    public DuplicateAddressException(String message) {
        super(message);
    }
}