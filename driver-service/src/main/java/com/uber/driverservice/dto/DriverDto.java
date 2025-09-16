package com.uber.driverservice.dto;
import lombok.Builder;
import lombok.Data;
@Data
@Builder
public class DriverDto {
    private Long id;
    private String username;
    private String phone;
    private Double latitude;
    private Double longitude;
}