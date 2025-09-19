package com.uber.paymentservice.controller;

import com.uber.paymentservice.dto.*;
import com.uber.paymentservice.entity.Payment;
import com.uber.paymentservice.service.PaymentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@Slf4j
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    // This is for initiating the transaction

    @PostMapping("/initiate")
    public ResponseEntity<InitiatePaymentResponse> initiatePayment(@Valid @RequestBody InitiatePaymentRequest request) {
        log.info("Received initiate payment request for rideId: {}", request.getRideId());
        InitiatePaymentResponse response = paymentService.initiatePayment(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Frontend-facing API: Creates a Razorpay order. Used by Mobile/Web App.
     * POST /api/payments/pay
     */
    @PostMapping("/pay")
    public ResponseEntity<PaymentOrderResponse> createPaymentOrder(@RequestParam(name = "rideId") String  rideId) {
        log.info("Received create payment order request for rideId: {}", rideId);
        PaymentOrderResponse response = paymentService.createPaymentOrder(rideId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // DTO for Create Payment Order Request (simple, as per the initial design)
    @Getter
    @Setter // Lombok annotations
    static class CreatePaymentOrderRequest {
        @NotBlank(message = "Ride ID cannot be blank")
        private String rideId;
        @NotNull(message = "User ID cannot be null")
        private Long userId;
        @NotNull(message = "Driver ID cannot be null")
        private Long driverId;
    }


    /**
     * Frontend-facing API: Verifies the payment after Razorpay checkout. Used by Mobile/Web App.
     * POST /api/payments/verify
     */
    @PostMapping("/verify")
    public ResponseEntity<Void> verifyPayment(@Valid @RequestBody VerifyPaymentRequest request) {
        log.info("Received verify payment request for orderId: {}", request.getRazorpay_order_id());
        paymentService.verifyPayment(request); // Service handles success/failure internally
        return new ResponseEntity<>(HttpStatus.OK); // Return 200 OK if verification process completed (success or failed)
    }

    /**
     * Internal API: Gets the status of a payment. Used by Ride/User/Driver Services.
     * GET /api/payments/{rideId}
     */

    @GetMapping("/{rideId}")
    public ResponseEntity<Payment> getPaymentStatus(@PathVariable String rideId) {
        log.info("Received get payment status request for rideId: {}", rideId);
        Payment payment = paymentService.getPaymentStatus(rideId);
        return new ResponseEntity<>(payment, HttpStatus.OK);
    }

    /**
     * Internal API: Refunds a payment. Used by Ride Service (on ride cancellation).
     * POST /api/payments/refund
     */
    @PostMapping("/refund")
    public ResponseEntity<RefundResponse> refundPayment(@Valid @RequestBody RefundRequest request) {
        log.info("Received refund payment request for paymentId: {}", request.getPaymentId());
        RefundResponse response = paymentService.refundPayment(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}