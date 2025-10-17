package com.uber.notificationservice.service;

import com.uber.notificationservice.dto.NotificationRequest;
import com.uber.notificationservice.dto.NotificationResponse;
import com.uber.notificationservice.entity.Notification;
import com.uber.notificationservice.enums.NotificationStatus;
import com.uber.notificationservice.enums.NotificationType;
import com.uber.notificationservice.enums.RecipientType;
import com.uber.notificationservice.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    @Override
    public void createNotification(NotificationRequest request) {
        RecipientType recipientType;
        NotificationType notificationType;
        // Checking recipient type from the request weather the Recipient Type is Valid or not
        try {
            recipientType = RecipientType.valueOf(request.getRecipientType().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid recipient type: " + request.getRecipientType());
        }
        // Checking Notification type from the request weather the Recipient Type is Valid or not
        try {
            notificationType = NotificationType.valueOf(request.getNotificationType().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid notification type: " + request.getNotificationType());
        }

        Notification notification = new Notification();
        notification.setRecipientId(request.getRecipientId());
        notification.setRecipientType(recipientType);
        notification.setNotificationType(notificationType);
        notification.setMessageContent(request.getMessageContent());
        notification.setCreatedAt(LocalDateTime.now());
        notification.setStatus(NotificationStatus.UNREAD);
        Notification savedNotification = notificationRepository.save(notification);
        System.out.println(savedNotification.toString());
        return ;
    }
    @Override
    public List<NotificationResponse> getNotificationsForRecipient(String recipientId, String recipientTypeString) {
        RecipientType recipientType;
        try {
            recipientType = RecipientType.valueOf(recipientTypeString.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid recipient type: " + recipientTypeString);
        }
        List<Notification> notifications = notificationRepository.findByRecipientIdAndRecipientTypeOrderByCreatedAtDesc(recipientId, recipientType);
        return notifications.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public NotificationResponse markNotificationAsRead(UUID notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found with ID: " + notificationId));
        notification.setStatus(NotificationStatus.READ);
        Notification updatedNotification = notificationRepository.save(notification);
        return mapToResponse(updatedNotification);
    }

    @Override
    public Long getUnreadNotificationCount(String recipientId, String recipientType) {
        return notificationRepository.findUnreadNotificationCountByRecipientIdAndRecipientType(recipientId, RecipientType.valueOf(recipientType));
    }

    private NotificationResponse mapToResponse(Notification notification) {
        NotificationResponse response = new NotificationResponse();
        response.setNotificationId(notification.getNotificationId());
        response.setRecipientId(notification.getRecipientId());
        response.setRecipientType(notification.getRecipientType().name());
        response.setNotificationType(notification.getNotificationType().name());
        response.setMessageContent(notification.getMessageContent());
        response.setCreatedAt(notification.getCreatedAt());
        response.setStatus(notification.getStatus().name());
        return response;
    }
}