package com.uber.notificationservice.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
public class NotificationRequest {
    @NotBlank(message = "Recipient ID cannot be empty")
    private String recipientId;
    @NotBlank(message = "Recipient type cannot be empty")
    private String recipientType; // Accepted as String, mapped to Enum internally
    @NotBlank(message = "Notification type cannot be empty")
    private String notificationType; // Accepted as String, mapped to Enum internally
    @NotBlank(message = "Message content cannot be empty")
    @Size(max = 1000, message = "Message content too long")
    private String messageContent;
}