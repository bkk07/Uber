package com.uber.paymentservice.service;

import com.razorpay.RazorpayException;
import com.uber.paymentservice.dto.*;
import com.uber.paymentservice.entity.Payment;
import com.uber.paymentservice.entity.PaymentStatus;
import com.uber.paymentservice.entity.Refund;
import com.uber.paymentservice.entity.RefundStatus;
import com.uber.paymentservice.exception.*;
import com.uber.paymentservice.external.razorpay.RazorpayClientWrapper;
import com.uber.paymentservice.repository.PaymentRepository;
import com.uber.paymentservice.repository.RefundRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final RefundRepository refundRepository;
    private final RazorpayClientWrapper razorpayClientWrapper;

    @Transactional
    public InitiatePaymentResponse initiatePayment(InitiatePaymentRequest request) {
        log.info("Initiating payment for rideId: {}", request.getRideId());

        // Check for existing payment for this ride to prevent duplicates (business logic)
        paymentRepository.findByRideId(request.getRideId()).ifPresent(p -> {
            if (p.getPaymentStatus() == PaymentStatus.PENDING) {
                throw new BusinessLogicException("Payment for ride " + request.getRideId() + " is already pending.");
            } else if (p.getPaymentStatus() == PaymentStatus.SUCCESS) {
                throw new BusinessLogicException("Payment for ride " + request.getRideId() + " has already succeeded.");
            }
            // If FAILED/REFUNDED, a new initiation might be allowed, depending on business rules.
            // For now, let's assume it's an error if one exists at all for simplicity.
            throw new BusinessLogicException("An existing payment record for ride " + request.getRideId() + " already exists with status: " + p.getPaymentStatus());
        });


        Payment payment = Payment.builder()
                .rideId(request.getRideId())
                .userId(request.getUserId())
                .driverId(request.getDriverId())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .paymentStatus(PaymentStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        payment = paymentRepository.save(payment);
        log.info("Payment record created with ID: {}", payment.getId());

        return new InitiatePaymentResponse(
                payment.getId(),
                payment.getRideId(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getPaymentStatus().name()
        );
    }
     // Frontend-facing API: Creates a Razorpay order after user starts payment.
     // Links the Razorpay order ID to the existing PENDING payment.
     // @param rideId The ride ID associated with the payment.
     // @return PaymentOrderResponse containing Razorpay order details.
    @Transactional
    public PaymentOrderResponse createPaymentOrder(String rideId) {
        log.info("Creating payment order for rideId: {}", rideId);
        Payment payment = paymentRepository.findByRideId(rideId)
                .orElseThrow(() -> new PaymentNotFoundException("rideId: " + rideId));

        if (payment.getPaymentStatus() != PaymentStatus.PENDING) {
            throw new InvalidPaymentStateException(
                    payment.getId(), payment.getPaymentStatus().name(), PaymentStatus.PENDING.name());
        }

        // Razorpay expects amount in the smallest currency unit (e.g., paise for INR)

        long amountInPaise = (long) (payment.getAmount() * 100);
        String razorpayOrderId;
        try {
            razorpayOrderId = razorpayClientWrapper.createOrder(amountInPaise, payment.getCurrency());
        } catch (RazorpayException e) {
            log.error("Failed to create Razorpay order for rideId {}: {}", rideId, e.getMessage());
            throw new BusinessLogicException("Failed to create payment order with Razorpay.", e);
        }
        payment.setOrderId(razorpayOrderId);
        payment.setUpdatedAt(LocalDateTime.now());
        paymentRepository.save(payment);
        log.info("Razorpay order created: {} for payment ID: {}", razorpayOrderId, payment.getId());
        return new PaymentOrderResponse(
                razorpayOrderId,
                (double) amountInPaise, // Return in paise, frontend will handle
                payment.getCurrency(),
                razorpayClientWrapper.getRazorpayKeyId()
        );
    }



    /**
     * Frontend-facing API: Verifies the payment after Razorpay checkout.
     * Updates the payment status to SUCCESS or FAILED based on verification.
     * @param request VerifyPaymentRequest DTO.
     * @return Status of the verification (success/failed).
     */
    @Transactional
    public String verifyPayment(VerifyPaymentRequest request) {
        log.info("Verifying payment for Razorpay order ID: {}", request.getRazorpay_order_id());
        Payment payment = paymentRepository.findByOrderId(request.getRazorpay_order_id())
                .orElseThrow(() -> new PaymentNotFoundException("orderId: " + request.getRazorpay_order_id()));

        if (payment.getPaymentStatus() != PaymentStatus.PENDING) {
            log.warn("Payment {} already processed with status: {}", payment.getId(), payment.getPaymentStatus());
            if (payment.getPaymentStatus() == PaymentStatus.SUCCESS) {
                return "success"; // Idempotency: if already success, return success
            }
            throw new InvalidPaymentStateException(
                    payment.getId(), payment.getPaymentStatus().name(), PaymentStatus.PENDING.name());
        }

        try {
            boolean isVerified = razorpayClientWrapper.verifyPayment(
                    request.getRazorpay_order_id(),
                    request.getRazorpay_payment_id(),
                    request.getRazorpay_signature()
            );

            if (isVerified) {
                payment.setPaymentStatus(PaymentStatus.SUCCESS);
                payment.setTransactionId(request.getRazorpay_payment_id());
                log.info("Payment {} successfully verified.", payment.getId());
                return "success";
            } else {
                payment.setPaymentStatus(PaymentStatus.FAILED);
                log.warn("Payment {} verification failed (Razorpay signature mismatch or other issue).", payment.getId());
                throw new PaymentVerificationException("Payment verification failed by Razorpay.");
            }
        } catch (PaymentVerificationException e) {
            payment.setPaymentStatus(PaymentStatus.FAILED); // Mark as failed on verification exception
            log.error("Error during payment verification for {}: {}", payment.getId(), e.getMessage());
            throw e; // Re-throw the custom exception
        } finally {
            payment.setUpdatedAt(LocalDateTime.now());
            paymentRepository.save(payment);
        }
    }

    /**
     * Internal API: Retrieves the status of a payment for a given ride ID.
     * @param rideId The ride ID.
     * @return Payment entity with its current status.
     */

    public Payment getPaymentStatus(String rideId) {
        log.debug("Fetching payment status for rideId: {}", rideId);
        return paymentRepository.findByRideId(rideId)
                .orElseThrow(() -> new PaymentNotFoundException("rideId: " + rideId));
    }

    /**
     * Internal API: Initiates a refund for a successful payment.
     * Creates a refund record and updates payment status if fully refunded.
     * @param request RefundRequest DTO.
     * @return RefundResponse DTO.
     */

    @Transactional
    public RefundResponse refundPayment(RefundRequest request) {
        log.info("Initiating refund for payment ID: {} with amount: {}", request.getPaymentId(), request.getAmount());
        Payment payment = paymentRepository.findById(request.getPaymentId())
                .orElseThrow(() -> new PaymentNotFoundException("id: " + request.getPaymentId()));

        if (payment.getPaymentStatus() != PaymentStatus.SUCCESS && payment.getPaymentStatus() != PaymentStatus.REFUNDED) {
            throw new InvalidPaymentStateException(
                    payment.getId(), payment.getPaymentStatus().name(), PaymentStatus.SUCCESS.name() + " or " + PaymentStatus.REFUNDED.name());
        }
        if (payment.getTransactionId() == null || payment.getTransactionId().isEmpty()) {
            throw new BusinessLogicException("Cannot refund payment " + payment.getId() + " as no external transaction ID is recorded.");
        }
        // Basic check if refund amount exceeds original amount (or remaining refundable amount)
        // More sophisticated logic would track total refunded amount.
        if (request.getAmount() > payment.getAmount()) {
            throw new BusinessLogicException("Refund amount " + request.getAmount() + " exceeds original payment amount " + payment.getAmount());
        }

        Refund refund = Refund.builder()
                .paymentId(payment.getId())
                .rideId(payment.getRideId())
                .amount(request.getAmount())
                .refundStatus(RefundStatus.INITIATED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        String externalRefundId;
        try {
            // Razorpay expects amount in the smallest currency unit (e.g., paise for INR)
            long amountInPaise = (long) (request.getAmount() * 100);
            externalRefundId = razorpayClientWrapper.initiateRefund(payment.getTransactionId(), amountInPaise);
            refund.setRefundStatus(RefundStatus.SUCCESS);
            refund.setRefundExternalId(externalRefundId);
            log.info("Refund for payment {} successfully initiated with external ID: {}", payment.getId(), externalRefundId);

            // For simplicity, if a refund is processed, mark the main payment as REFUNDED.
            // In a real scenario, you'd calculate remaining amount to allow partial refunds and update status if fully refunded.
            if (request.getAmount().equals(payment.getAmount())) { // Simple check for full refund
                payment.setPaymentStatus(PaymentStatus.REFUNDED);
                log.info("Payment {} fully refunded, updating payment status to REFUNDED.", payment.getId());
            } else {
                // Handle partial refund: keep SUCCESS but maybe add a 'partially_refunded' status or flag
                // For now, leave as SUCCESS if it's a partial refund
            }

        } catch (RazorpayException e) {
            refund.setRefundStatus(RefundStatus.FAILED);
            log.error("Failed to initiate refund for payment {}: {}", payment.getId(), e.getMessage());
            throw new RefundProcessingException("Failed to initiate refund with Razorpay.", e);
        } finally {
            refundRepository.save(refund);
            payment.setUpdatedAt(LocalDateTime.now());
            paymentRepository.save(payment);
        }

        return new RefundResponse(
                refund.getRefundExternalId() != null ? refund.getRefundExternalId() : "N/A",
                refund.getRefundStatus().name()
        );
    }
}