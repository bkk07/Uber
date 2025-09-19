package com.uber.paymentservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Payment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false) private String rideId;
    @Column(nullable = false) private Long userId;
    @Column(nullable = false) private Long driverId;

    @Column(nullable = false) private Double amount;
    @Column(nullable = false) private String currency;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod; // This would typically be set after external payment initiation

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    @Column(unique = true) private String transactionId; // Razorpay payment_id
    @Column(unique = true) private String orderId;       // Razorpay order_id, useful for verification

    @Column(nullable = false) private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}