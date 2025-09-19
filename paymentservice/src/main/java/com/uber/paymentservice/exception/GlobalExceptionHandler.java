package com.uber.paymentservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    // Handles @Valid DTO validation failures
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidationExceptions(MethodArgumentNotValidException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation failed for request payload.");
        problemDetail.setTitle("Bad Request");
        problemDetail.setType(URI.create("/errors/bad-request"));
        problemDetail.setProperty("timestamp", LocalDateTime.now());

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage()));
        problemDetail.setProperty("errors", errors);

        return ResponseEntity.of(problemDetail).build();
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ProblemDetail> handleNotFoundException(NotFoundException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problemDetail.setTitle("Resource Not Found");
        problemDetail.setType(URI.create("/errors/not-found"));
        problemDetail.setProperty("timestamp", LocalDateTime.now());
        return ResponseEntity.of(problemDetail).build();
    }

    @ExceptionHandler(InvalidPaymentStateException.class)
    public ResponseEntity<ProblemDetail> handleInvalidPaymentStateException(InvalidPaymentStateException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problemDetail.setTitle("Invalid Payment State");
        problemDetail.setType(URI.create("/errors/invalid-state"));
        problemDetail.setProperty("timestamp", LocalDateTime.now());
        return ResponseEntity.of(problemDetail).build();
    }

    @ExceptionHandler(PaymentVerificationException.class)
    public ResponseEntity<ProblemDetail> handlePaymentVerificationException(PaymentVerificationException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problemDetail.setTitle("Payment Verification Failed");
        problemDetail.setType(URI.create("/errors/payment-verification-failed"));
        problemDetail.setProperty("timestamp", LocalDateTime.now());
        return ResponseEntity.of(problemDetail).build();
    }

    @ExceptionHandler(RefundProcessingException.class)
    public ResponseEntity<ProblemDetail> handleRefundProcessingException(RefundProcessingException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Refund processing failed: " + ex.getMessage());
        problemDetail.setTitle("Refund Processing Error");
        problemDetail.setType(URI.create("/errors/refund-processing-failed"));
        problemDetail.setProperty("timestamp", LocalDateTime.now());
        return ResponseEntity.of(problemDetail).build();
    }

    @ExceptionHandler(BusinessLogicException.class)
    public ResponseEntity<ProblemDetail> handleBusinessLogicException(BusinessLogicException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problemDetail.setTitle("Business Logic Error");
        problemDetail.setType(URI.create("/errors/business-logic-error"));
        problemDetail.setProperty("timestamp", LocalDateTime.now());
        return ResponseEntity.of(problemDetail).build();
    }

    // Generic handler for any unhandled PaymentServiceException (if any specific wasn't caught)
    @ExceptionHandler(PaymentServiceException.class)
    public ResponseEntity<ProblemDetail> handlePaymentServiceException(PaymentServiceException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "A payment service error occurred: " + ex.getMessage());
        problemDetail.setTitle("Payment Service Error");
        problemDetail.setType(URI.create("/errors/payment-service-error"));
        problemDetail.setProperty("timestamp", LocalDateTime.now());
        return ResponseEntity.of(problemDetail).build();
    }


    // Catch-all for any other unexpected exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleGenericException(Exception ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred: " + ex.getMessage());
        problemDetail.setTitle("Internal Server Error");
        problemDetail.setType(URI.create("/errors/internal-server-error"));
        problemDetail.setProperty("timestamp", LocalDateTime.now());
        // In a real app, log the full stack trace here for debugging.
        ex.printStackTrace();
        return ResponseEntity.of(problemDetail).build();
    }
}