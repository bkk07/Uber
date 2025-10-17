package com.uber.notificationservice.repository;

import com.uber.notificationservice.entity.Notification;
import com.uber.notificationservice.enums.RecipientType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    List<Notification> findByRecipientIdAndRecipientTypeOrderByCreatedAtDesc(String recipientId, RecipientType recipientType);
    @Query("SELECT COUNT(n) FROM Notification n " +
            "WHERE n.recipientId = :recipientId " +
            "AND n.recipientType = :recipientType " +
            "AND n.status = com.uber.notificationservice.enums.NotificationStatus.UNREAD")
    Long findUnreadNotificationCountByRecipientIdAndRecipientType(
            @Param("recipientId") String recipientId,
            @Param("recipientType") RecipientType recipientType
    );
}