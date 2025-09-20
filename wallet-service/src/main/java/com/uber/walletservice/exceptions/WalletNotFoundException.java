package com.uber.walletservice.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND) // Maps to 404 Not Found
public class WalletNotFoundException extends WalletServiceException {
    public WalletNotFoundException(String message) {
        super(message);
    }
}