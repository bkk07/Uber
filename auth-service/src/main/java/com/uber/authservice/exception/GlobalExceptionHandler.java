package com.uber.authservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    // Handles both ResourceNotFoundException and InvalidCredentialsException (both 401/UNAUTHORIZED)
    @ExceptionHandler({ResourceNotFoundException.class, InvalidCredentialsException.class})
    public ResponseEntity<Object> handleAuthenticationExceptions(RuntimeException ex, WebRequest request) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", "Authentication Failed");

        // **This is the crucial part: sending the User Service's error message**
        body.put("message", ex.getMessage());
        body.put("path", request.getDescription(false).replace("uri=", ""));

        return new ResponseEntity<>(body, status);
    }

}