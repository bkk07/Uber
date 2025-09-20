package com.uber.paymentservice.repository;

import com.uber.paymentservice.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByRideId(String rideId);
    Optional<Payment> findByOrderId(String orderId);
    List<Payment> findByUserId(Long userId);
    List<Payment> findByDriverId(Long driverId);
}