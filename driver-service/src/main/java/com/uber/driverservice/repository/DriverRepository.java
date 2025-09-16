package com.uber.driverservice.repository;

import com.uber.driverservice.enums.DriverStatus;
import com.uber.driverservice.model.Driver;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DriverRepository extends JpaRepository<Driver, Long> {
    Optional<Driver> findByEmail(String email);
    boolean existsByEmail(String email);
    List<Driver> findByStatus(DriverStatus status); // Custom method to get all available drivers

    Optional<Object> findByUsername(String username);

    boolean existsByUsername(@NotBlank(message = "Name cannot be blank") @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters") String username);
}
