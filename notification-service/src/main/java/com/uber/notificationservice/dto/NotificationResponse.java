package com.uber.notificationservice.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class NotificationResponse {
    private UUID notificationId;
    private String recipientId;
    private String recipientType;
    private String notificationType;
    private String messageContent;
    private LocalDateTime createdAt;
    private String status; // Exposed as String
}