package com.uber.rideservice.dto;

import lombok.Data;

// This DTO would match the structure returned by Driver-Service for a driver
@Data
public class DriverDto {
    private Long id;
    private String username;
    private String phone;
    private Double latitude;
    private Double longitude;
}