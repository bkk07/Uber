package com.uber.paymentservice.controller;

import com.uber.paymentservice.dto.*;
import com.uber.paymentservice.entity.Payment;
import com.uber.paymentservice.service.PaymentService;
import com.uber.paymentservice.exception.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@Slf4j
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:5500", "http://localhost:8089"}) // Add your frontend URLs
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/initiate")
    public ResponseEntity<InitiatePaymentResponse> initiatePayment(@Valid @RequestBody InitiatePaymentRequest request) {
        try {
            log.info("Received initiate payment request for rideId: {}", request.getRideId());
            InitiatePaymentResponse response = paymentService.initiatePayment(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (BusinessLogicException e) {
            log.error("Business logic error during payment initiation: {}", e.getMessage());
            throw e; // Let global exception handler deal with it
        } catch (Exception e) {
            log.error("Unexpected error during payment initiation for rideId {}: {}", request.getRideId(), e.getMessage());
            throw new BusinessLogicException("Failed to initiate payment", e);
        }
    }

    @PostMapping("/pay")
    public ResponseEntity<PaymentOrderResponse> createPaymentOrder(@RequestParam(name = "rideId") String rideId) {
        try {
            log.info("Received create payment order request for rideId: {}", rideId);

            // Validate input
            if (rideId == null || rideId.trim().isEmpty()) {
                log.error("Invalid rideId provided: {}", rideId);
                throw new BusinessLogicException("RideId cannot be null or empty");
            }

            PaymentOrderResponse response = paymentService.createPaymentOrder(rideId);
            log.info("Payment order created successfully for rideId: {}", rideId);
            return ResponseEntity.ok(response);

        } catch (PaymentNotFoundException e) {
            log.error("Payment not found for rideId {}: {}", rideId, e.getMessage());
            throw e;
        } catch (InvalidPaymentStateException e) {
            log.error("Invalid payment state for rideId {}: {}", rideId, e.getMessage());
            throw e;
        } catch (BusinessLogicException e) {
            log.error("Business logic error creating payment order for rideId {}: {}", rideId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error creating payment order for rideId {}: {}", rideId, e.getMessage());
            throw new BusinessLogicException("Failed to create payment order", e);
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<Map<String, Object>> verifyPayment(@RequestBody Map<String, String> payload) {
        Map<String, Object> response = new HashMap<>();

        try {
            log.info("Received payment verification request");

            // Log the payload for debugging (be careful with sensitive data in production)
            log.debug("Verification payload keys: {}", payload.keySet());

            String status = paymentService.verifyPayment(payload);

            response.put("status", status);
            if ("success".equals(status)) {
                response.put("message", "Payment verified successfully");
                response.put("paymentId", payload.get("razorpay_payment_id"));
            } else {
                response.put("message", "Payment verification failed");
            }

            log.info("Payment verification completed with status: {}", status);
            return ResponseEntity.ok(response);

        } catch (PaymentVerificationException e) {
            log.error("Payment verification failed: {}", e.getMessage());
            response.put("status", "failed");
            response.put("message", e.getMessage());
            response.put("error_code", "VERIFICATION_FAILED");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);

        } catch (PaymentNotFoundException e) {
            log.error("Payment not found during verification: {}", e.getMessage());
            response.put("status", "failed");
            response.put("message", "Payment record not found");
            response.put("error_code", "PAYMENT_NOT_FOUND");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);

        } catch (InvalidPaymentStateException e) {
            log.error("Invalid payment state during verification: {}", e.getMessage());
            response.put("status", "failed");
            response.put("message", e.getMessage());
            response.put("error_code", "INVALID_PAYMENT_STATE");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);

        } catch (Exception e) {
            log.error("Unexpected error during payment verification: {}", e.getMessage(), e);
            response.put("status", "error");
            response.put("message", "An unexpected error occurred during payment verification");
            response.put("error_code", "INTERNAL_ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/{rideId}")
    public ResponseEntity<Payment> getPaymentStatus(@PathVariable String rideId) {
        try {
            log.info("Received get payment status request for rideId: {}", rideId);

            if (rideId == null || rideId.trim().isEmpty()) {
                throw new BusinessLogicException("RideId cannot be null or empty");
            }

            Payment payment = paymentService.getPaymentStatus(rideId);
            return ResponseEntity.ok(payment);

        } catch (PaymentNotFoundException e) {
            log.error("Payment not found for rideId {}: {}", rideId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error fetching payment status for rideId {}: {}", rideId, e.getMessage());
            throw new BusinessLogicException("Failed to fetch payment status", e);
        }
    }

    @PostMapping("/refund")
    public ResponseEntity<RefundResponse> refundPayment(@Valid @RequestBody RefundRequest request) {
        try {
            log.info("Received refund payment request for paymentId: {}", request.getPaymentId());
            RefundResponse response = paymentService.refundPayment(request);
            return ResponseEntity.ok(response);

        } catch (PaymentNotFoundException e) {
            log.error("Payment not found for refund request: {}", e.getMessage());
            throw e;
        } catch (InvalidPaymentStateException e) {
            log.error("Invalid payment state for refund: {}", e.getMessage());
            throw e;
        } catch (RefundProcessingException e) {
            log.error("Refund processing failed: {}", e.getMessage());
            throw e;
        } catch (BusinessLogicException e) {
            log.error("Business logic error during refund: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during refund for paymentId {}: {}", request.getPaymentId(), e.getMessage());
            throw new BusinessLogicException("Failed to process refund", e);
        }
    }

    // Health check endpoint
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "payment-service");
        return ResponseEntity.ok(response);
    }
}