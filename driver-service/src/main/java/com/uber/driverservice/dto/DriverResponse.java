package com.uber.driverservice.dto;
import com.uber.driverservice.enums.DriverStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DriverResponse {
    private Long id;
    private String username;
    private String email;
    private String phone;
    private DriverStatus status;
    private double latitude;
    private double longitude;
    private LocalDateTime createdAt;
    private Long perKilometer;
    private String vehicle;
}