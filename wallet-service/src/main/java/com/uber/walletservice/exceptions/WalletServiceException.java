package com.uber.walletservice.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// Base custom exception for Wallet Service
public class WalletServiceException extends RuntimeException {
    public WalletServiceException(String message) {
        super(message);
    }
}