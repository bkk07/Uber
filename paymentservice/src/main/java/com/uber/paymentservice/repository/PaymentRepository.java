package com.uber.paymentservice.repository;

import com.uber.paymentservice.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByRideId(String rideId);
    Optional<Payment> findByOrderId(String orderId);
}