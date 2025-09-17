package com.uber.rideservice.feignclient;

import com.uber.rideservice.dto.NotificationRequest;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "notification-service")
public interface NotificationServiceFeignClient {
    @PostMapping("/api/v1/notifications")
    public void createNotification(@Valid @RequestBody NotificationRequest request);
}