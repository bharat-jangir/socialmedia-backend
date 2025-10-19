package com.bharat.springbootsocial.services;

import com.bharat.springbootsocial.entity.Notification;
import com.bharat.springbootsocial.entity.User;
import com.bharat.springbootsocial.exception.UserException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface NotificationService {
    
    // Create and send notification
    Notification createAndSendNotification(User recipient, User sender, Notification.NotificationType type, 
                                         String title, String message, String relatedEntityType, UUID relatedEntityId);
    
    // Get notifications for user
    List<Notification> getNotificationsByUserId(UUID userId);
    
    // Get paginated notifications for user
    Page<Notification> getNotificationsByUserIdPaginated(UUID userId, Pageable pageable);
    
    // Get unread notifications for user
    List<Notification> getUnreadNotificationsByUserId(UUID userId);
    
    // Get unread notification count
    Integer getUnreadNotificationCount(UUID userId);
    
    // Mark notification as read
    void markNotificationAsRead(UUID notificationId, User user) throws UserException;
    
    // Mark all notifications as read for user
    void markAllNotificationsAsRead(UUID userId);
    
    // Delete notification
    void deleteNotification(UUID notificationId, User user) throws UserException;
    
    // Delete all notifications for user
    void deleteAllNotifications(UUID userId);
    
    // Get notifications by type
    List<Notification> getNotificationsByType(UUID userId, Notification.NotificationType type);
    
    // Clean up old notifications
    void cleanupOldNotifications();
    
    // Send real-time notification via WebSocket
    void sendRealTimeNotification(User recipient, Notification notification);
    
    // Specific notification methods
    void sendLikeNotification(User recipient, User sender, String entityType, UUID entityId);
    void sendCommentNotification(User recipient, User sender, String entityType, UUID entityId);
    void sendFollowNotification(User recipient, User sender);
    void sendGroupInvitationNotification(User recipient, User sender, com.bharat.springbootsocial.entity.Group group);
    
    void sendCallInvitationNotification(User recipient, User sender, Object callRoom);
    void sendCallResponseNotification(User recipient, User sender, UUID roomId, boolean accepted);
}
