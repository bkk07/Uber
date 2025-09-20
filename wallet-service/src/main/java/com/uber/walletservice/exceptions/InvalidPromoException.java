package com.uber.walletservice.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST) // Maps to 400 Bad Request
public class InvalidPromoException extends WalletServiceException {
    public InvalidPromoException(String message) {
        super(message);
    }
}