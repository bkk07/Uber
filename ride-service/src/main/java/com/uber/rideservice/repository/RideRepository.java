package com.uber.rideservice.repository;

import com.uber.rideservice.entity.Ride;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RideRepository extends JpaRepository<Ride, Long> {
    // Custom query methods can be added here if needed
}