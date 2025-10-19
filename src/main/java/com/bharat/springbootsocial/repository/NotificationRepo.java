package com.bharat.springbootsocial.repository;

import com.bharat.springbootsocial.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface NotificationRepo extends JpaRepository<Notification, UUID> {
    
    // Find notifications by recipient
    List<Notification> findByRecipientIdOrderByCreatedAtDesc(UUID recipientId);
    
    // Find paginated notifications by recipient
    Page<Notification> findByRecipientIdOrderByCreatedAtDesc(UUID recipientId, Pageable pageable);
    
    // Find unread notifications by recipient
    List<Notification> findByRecipientIdAndIsReadFalseOrderByCreatedAtDesc(UUID recipientId);
    
    // Count unread notifications by recipient
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.recipient.id = :recipientId AND n.isRead = false")
    Integer countUnreadNotificationsByRecipientId(@Param("recipientId") UUID recipientId);
    
    // Find notifications by type and recipient
    List<Notification> findByRecipientIdAndTypeOrderByCreatedAtDesc(UUID recipientId, Notification.NotificationType type);
    
    // Find notifications by related entity
    @Query("SELECT n FROM Notification n WHERE n.relatedEntityType = :entityType AND n.relatedEntityId = :entityId")
    List<Notification> findByRelatedEntity(@Param("entityType") String entityType, @Param("entityId") UUID entityId);
    
    // Find recent notifications (last 30 days)
    @Query("SELECT n FROM Notification n WHERE n.recipient.id = :recipientId AND n.createdAt >= :since ORDER BY n.createdAt DESC")
    List<Notification> findRecentNotificationsByRecipientId(@Param("recipientId") UUID recipientId, @Param("since") LocalDateTime since);
    
    // Delete old notifications (older than specified days)
    @Query("DELETE FROM Notification n WHERE n.createdAt < :cutoffDate")
    void deleteOldNotifications(@Param("cutoffDate") LocalDateTime cutoffDate);
}
