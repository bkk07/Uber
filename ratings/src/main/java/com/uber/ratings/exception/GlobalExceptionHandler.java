package com.uber.ratings.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDetails> handleValidationExceptions( // Changed Object to ErrorDetails for specific return type
                                                                    MethodArgumentNotValidException ex, WebRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage()));

        ErrorDetails errorDetails = new ErrorDetails(
                LocalDateTime.now(),            // Argument 1: timestamp
                "Validation Failed",            // Argument 2: message (general purpose)
                errors.toString(),              // Argument 3: details (specific validation error messages)
                request.getDescription(false)   // Argument 4: description (request URI/details)
        );
        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorDetails> handleResourceNotFoundException(ResourceNotFoundException ex, WebRequest request) {
        ErrorDetails errorDetails = new ErrorDetails(
                LocalDateTime.now(),            // Argument 1: timestamp
                "Resource Not Found",           // Argument 2: message (general purpose)
                ex.getMessage(),                // Argument 3: details (specific message from the exception)
                request.getDescription(false)   // Argument 4: description (request URI/details)
        );
        return new ResponseEntity<>(errorDetails, HttpStatus.NOT_FOUND);
    }

    // Generic exception handler for all other unhandled exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDetails> handleGlobalException(Exception ex, WebRequest request) {
        ErrorDetails errorDetails = new ErrorDetails(
                LocalDateTime.now(),                            // Argument 1: timestamp
                "An unexpected error occurred",                 // Argument 2: message (general purpose for security)
                ex.getMessage(),                                // Argument 3: details (specific message from the exception)
                request.getDescription(false)                   // Argument 4: description (request URI/details)
        );
        return new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}