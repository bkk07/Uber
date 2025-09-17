package com.uber.rideservice.feignclient;

import com.uber.rideservice.config.FeignClientConfig;
import com.uber.rideservice.dto.DriverDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
@FeignClient(name = "driver-service" , configuration = FeignClientConfig.class)
public interface DriverServiceFeignClient {
    @GetMapping("/api/drivers/nearby")
    List<DriverDto> getNearbyDrivers(
            @RequestParam("latitude") Double latitude,
            @RequestParam("longitude") Double longitude,
            @RequestParam(value = "limit", defaultValue = "5") int limit);

    @PostMapping("/api/drivers/{driverId}/status")
    void updateDriverStatus(@PathVariable("driverId") Long driverId, @RequestParam("status") String status);
    // Additional methods for driver availability etc. might be needed
}