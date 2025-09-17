package com.uber.notificationservice.repository;

import com.uber.notificationservice.entity.Notification;
import com.uber.notificationservice.enums.RecipientType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    List<Notification> findByRecipientIdAndRecipientTypeOrderByCreatedAtDesc(String recipientId, RecipientType recipientType);
}