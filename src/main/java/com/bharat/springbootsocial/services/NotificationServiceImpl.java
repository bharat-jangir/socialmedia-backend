package com.bharat.springbootsocial.services;

import com.bharat.springbootsocial.entity.Notification;
import com.bharat.springbootsocial.entity.User;
import com.bharat.springbootsocial.exception.UserException;
import com.bharat.springbootsocial.repository.NotificationRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationServiceImpl.class);
    
    @Autowired
    private NotificationRepo notificationRepo;
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    @Override
    @Transactional
    public Notification createAndSendNotification(User recipient, User sender, Notification.NotificationType type, 
                                                 String title, String message, String relatedEntityType, UUID relatedEntityId) {
        Notification notification = new Notification();
        notification.setRecipient(recipient);
        notification.setSender(sender);
        notification.setType(type);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setRelatedEntityType(relatedEntityType);
        notification.setRelatedEntityId(relatedEntityId);
        notification.setIsRead(false);
        notification.setCreatedAt(LocalDateTime.now());
        
        Notification savedNotification = notificationRepo.save(notification);
        
        // Send real-time notification via WebSocket
        sendRealTimeNotification(recipient, savedNotification);
        
        return savedNotification;
    }
    
    // Overloaded method for Long entity IDs (for Story entities)
    public Notification createAndSendNotification(User recipient, User sender, Notification.NotificationType type, 
                                                 String title, String message, String relatedEntityType, Long relatedEntityId) {
        // Convert Long to UUID by creating a UUID from the Long value
        // Note: This is a simple conversion, but in a real application you might want a more sophisticated approach
        UUID uuidEntityId = UUID.nameUUIDFromBytes(relatedEntityId.toString().getBytes());
        return createAndSendNotification(recipient, sender, type, title, message, relatedEntityType, uuidEntityId);
    }
    
    @Override
    public List<Notification> getNotificationsByUserId(UUID userId) {
        return notificationRepo.findByRecipientIdOrderByCreatedAtDesc(userId);
    }
    
    @Override
    public Page<Notification> getNotificationsByUserIdPaginated(UUID userId, Pageable pageable) {
        return notificationRepo.findByRecipientIdOrderByCreatedAtDesc(userId, pageable);
    }
    
    @Override
    public List<Notification> getUnreadNotificationsByUserId(UUID userId) {
        return notificationRepo.findByRecipientIdAndIsReadFalseOrderByCreatedAtDesc(userId);
    }
    
    @Override
    public Integer getUnreadNotificationCount(UUID userId) {
        return notificationRepo.countUnreadNotificationsByRecipientId(userId);
    }
    
    @Override
    @Transactional
    public void markNotificationAsRead(UUID notificationId, User user) throws UserException {
        Notification notification = notificationRepo.findById(notificationId)
                .orElseThrow(() -> new UserException("Notification not found"));
        
        if (!notification.getRecipient().getId().equals(user.getId())) {
            throw new UserException("You can only mark your own notifications as read");
        }
        
        notification.setIsRead(true);
        notification.setReadAt(LocalDateTime.now());
        notificationRepo.save(notification);
    }
    
    @Override
    @Transactional
    public void markAllNotificationsAsRead(UUID userId) {
        List<Notification> unreadNotifications = notificationRepo.findByRecipientIdAndIsReadFalseOrderByCreatedAtDesc(userId);
        for (Notification notification : unreadNotifications) {
            notification.setIsRead(true);
            notification.setReadAt(LocalDateTime.now());
        }
        notificationRepo.saveAll(unreadNotifications);
    }
    
    @Override
    @Transactional
    public void deleteNotification(UUID notificationId, User user) throws UserException {
        Notification notification = notificationRepo.findById(notificationId)
                .orElseThrow(() -> new UserException("Notification not found"));
        
        if (!notification.getRecipient().getId().equals(user.getId())) {
            throw new UserException("You can only delete your own notifications");
        }
        
        notificationRepo.delete(notification);
    }
    
    @Override
    @Transactional
    public void deleteAllNotifications(UUID userId) {
        List<Notification> userNotifications = notificationRepo.findByRecipientIdOrderByCreatedAtDesc(userId);
        notificationRepo.deleteAll(userNotifications);
        logger.info("Deleted all notifications for user ID: {}", userId);
    }
    
    @Override
    public List<Notification> getNotificationsByType(UUID userId, Notification.NotificationType type) {
        return notificationRepo.findByRecipientIdAndTypeOrderByCreatedAtDesc(userId, type);
    }
    
    @Override
    @Scheduled(cron = "0 0 2 * * ?") // Run daily at 2 AM
    @Transactional
    public void cleanupOldNotifications() {
        int daysToKeep = 30; // Keep notifications for 30 days
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysToKeep);
        notificationRepo.deleteOldNotifications(cutoffDate);
    }
    
    @Override
    public void sendRealTimeNotification(User recipient, Notification notification) {
        try {
            // Send notification to specific user via WebSocket
            String destination = "/user/" + recipient.getId() + "/queue/notifications";
            System.out.println("Sending WebSocket notification to: " + destination);
            System.out.println("Notification: " + notification.getTitle() + " - " + notification.getMessage());
            messagingTemplate.convertAndSend(destination, notification);
            System.out.println("WebSocket notification sent successfully!");
        } catch (Exception e) {
            // Log error but don't fail the notification creation
            System.err.println("Failed to send real-time notification: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Helper methods for different notification types
    public void sendLikeNotification(User postOwner, User liker, String entityType, UUID entityId) {
        if (!postOwner.getId().equals(liker.getId())) { // Don't notify self
            String title = "New Like";
            String message = liker.getFname() + " " + liker.getLname() + " liked your " + entityType.toLowerCase();
            createAndSendNotification(postOwner, liker, 
                entityType.equals("POST") ? Notification.NotificationType.LIKE_POST : Notification.NotificationType.LIKE_STORY,
                title, message, entityType, entityId);
        }
    }
    
    public void sendCommentNotification(User postOwner, User commenter, String entityType, UUID entityId) {
        if (!postOwner.getId().equals(commenter.getId())) { // Don't notify self
            String title = "New Comment";
            String message = commenter.getFname() + " " + commenter.getLname() + " commented on your " + entityType.toLowerCase();
            createAndSendNotification(postOwner, commenter, 
                entityType.equals("POST") ? Notification.NotificationType.COMMENT_POST : Notification.NotificationType.COMMENT_STORY,
                title, message, entityType, entityId);
        }
    }
    
    public void sendFollowNotification(User userBeingFollowed, User follower) {
        if (!userBeingFollowed.getId().equals(follower.getId())) { // Don't notify self
            String title = "New Follower";
            String message = follower.getFname() + " " + follower.getLname() + " started following you";
            createAndSendNotification(userBeingFollowed, follower, Notification.NotificationType.FOLLOW,
                title, message, "USER", follower.getId());
        }
    }
    
    public void sendGroupInvitationNotification(User recipient, User sender, com.bharat.springbootsocial.entity.Group group) {
        if (!recipient.getId().equals(sender.getId())) { // Don't notify self
            String title = "Group Invitation";
            String message = sender.getFname() + " " + sender.getLname() + " invited you to join the group \"" + group.getName() + "\"";
            createAndSendNotification(recipient, sender, Notification.NotificationType.GROUP_INVITATION,
                title, message, "GROUP", group.getId());
        }
    }
    
    public void sendCallInvitationNotification(User recipient, User sender, Object callRoom) {
        logger.info("Sending call invitation notification - Recipient ID: {}, Sender ID: {}", recipient.getId(), sender.getId());
        
        if (!recipient.getId().equals(sender.getId())) { // Don't notify self
            String title = "Incoming Call";
            String message = sender.getFname() + " " + sender.getLname() + " is calling you";
            
            // Extract room ID from callRoom object
            UUID roomId = null;
            if (callRoom instanceof com.bharat.springbootsocial.entity.CallRoom) {
                com.bharat.springbootsocial.entity.CallRoom room = (com.bharat.springbootsocial.entity.CallRoom) callRoom;
                roomId = room.getId(); // Use the UUID id of the room
                logger.info("Extracted room ID from CallRoom: {}", roomId);
            } else {
                logger.warn("CallRoom object is not of expected type: {}", callRoom != null ? callRoom.getClass().getName() : "null");
            }
            
            createAndSendNotification(recipient, sender, Notification.NotificationType.CALL_INVITATION,
                title, message, "CALL", roomId);
            logger.info("Call invitation notification sent successfully to user {} via notification system with room ID: {}", recipient.getId(), roomId);
        } else {
            logger.info("Skipping notification - sender and recipient are the same user");
        }
    }
    
    // Send call response notification (accept/decline)
    public void sendCallResponseNotification(User recipient, User sender, UUID roomId, boolean accepted) {
        logger.info("🔔 Sending call response notification - Recipient ID: {}, Sender ID: {}, Accepted: {}, RoomId: {}", 
            recipient.getId(), sender.getId(), accepted, roomId);
        
        if (!recipient.getId().equals(sender.getId())) { // Don't notify self
            String title = accepted ? "Call Accepted" : "Call Declined";
            String message = sender.getFname() + " " + sender.getLname() + (accepted ? " accepted your call" : " declined your call");
            
            logger.info("🔔 Creating notification - Title: {}, Message: {}, Type: CALL_RESPONSE", title, message);
            logger.info("🔔 Notification recipient details - ID: {}, Name: {}, Email: {}", 
                recipient.getId(), recipient.getFname() + " " + recipient.getLname(), recipient.getEmail());
            Notification notification = createAndSendNotification(recipient, sender, Notification.NotificationType.CALL_RESPONSE,
                title, message, "CALL", roomId);
            logger.info("🔔 Call response notification created and sent successfully - Notification ID: {}, Recipient: {}, RoomId: {}", 
                notification.getId(), recipient.getId(), roomId);
            logger.info("🔔 Notification will be sent to WebSocket destination: /user/{}/queue/notifications", recipient.getId());
        } else {
            logger.info("🔔 Skipping notification - sender and recipient are the same user");
        }
    }
    
}
