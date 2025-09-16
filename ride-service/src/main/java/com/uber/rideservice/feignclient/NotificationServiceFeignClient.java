package com.uber.rideservice.feignclient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

// The 'name' should match the service ID in Eureka/Consul or a logical name
// The 'url' property will be picked from application.properties: notification-service.url
@FeignClient(name = "notification-service", url = "${notification-service.url}")
public interface NotificationServiceFeignClient {
    @PostMapping("/api/notifications/send")
    void sendNotification(@RequestBody NotificationRequest notificationRequest);
    // Inner class for NotificationRequest DTO
    class NotificationRequest {
        public Long recipientId; // userId or driverId
        public String type;      // e.g., "RIDE_REQUEST", "RIDE_CONFIRMED"
        public String message;
        // Optionally, add more fields like rideId, details, etc.
        public NotificationRequest(Long recipientId, String type, String message) {
            this.recipientId = recipientId;
            this.type = type;
            this.message = message;
        }
    }
}