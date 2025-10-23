package com.uber.rideservice.repository;

import com.uber.rideservice.entity.Ride;
import com.uber.rideservice.entity.RideStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RideRepository extends JpaRepository<Ride, Long> {
    List<Ride> findByUserIdAndStatusIn(Long userId, List<RideStatus> completed);

    List<Ride> findByDriverIdAndStatusIn(Long driverId, List<RideStatus> completed);
}