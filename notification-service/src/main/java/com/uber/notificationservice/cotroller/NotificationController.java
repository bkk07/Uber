package com.uber.notificationservice.cotroller;

import com.uber.notificationservice.dto.NotificationRequest;
import com.uber.notificationservice.dto.NotificationResponse;
import com.uber.notificationservice.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    // Creating the Notification
    @PostMapping
    public ResponseEntity<Void> createNotification(@Valid @RequestBody NotificationRequest request) {
        notificationService.createNotification(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // Get Notifications By the User or Driver
    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getNotificationsForRecipient(
            @RequestParam @Valid String recipientId,
            @RequestParam @Valid String recipientType) {
        List<NotificationResponse> notifications = notificationService.getNotificationsForRecipient(recipientId, recipientType);
        return ResponseEntity.ok(notifications);
    }

        // Marking notification as Read
    @PutMapping("/{notificationId}/read")
    public ResponseEntity<NotificationResponse> markNotificationAsRead(@PathVariable UUID notificationId) {
        NotificationResponse updatedNotification = notificationService.markNotificationAsRead(notificationId);
        return ResponseEntity.ok(updatedNotification);
    }
    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadNotificationCount(@RequestParam String recipientId, @RequestParam String recipientType){
        Long unreadCount = notificationService.getUnreadNotificationCount(recipientId, recipientType);
        return ResponseEntity.ok(unreadCount);

    }

}