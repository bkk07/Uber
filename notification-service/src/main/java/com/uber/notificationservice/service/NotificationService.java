package com.uber.notificationservice.service;

import com.uber.notificationservice.dto.NotificationRequest;
import com.uber.notificationservice.dto.NotificationResponse;
import java.util.List;
import java.util.UUID;

public interface NotificationService {
    void createNotification(NotificationRequest request);
    List<NotificationResponse> getNotificationsForRecipient(String recipientId, String recipientType);
    NotificationResponse markNotificationAsRead(UUID notificationId);

    Long getUnreadNotificationCount(String recipientId, String recipientType);
}