package com.uber.paymentservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "refunds")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Refund {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false) private Long paymentId;   // FK → Payment.id
    @Column(nullable = false) private String rideId;
    @Column(nullable = false) private Double amount;

    @Enumerated(EnumType.STRING)
    private RefundStatus refundStatus;

    private String refundExternalId; // ID from payment gateway (e.g., Razorpay refund ID)

    @Column(nullable = false) private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}