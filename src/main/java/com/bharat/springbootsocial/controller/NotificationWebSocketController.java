package com.bharat.springbootsocial.controller;

import com.bharat.springbootsocial.entity.User;
import com.bharat.springbootsocial.services.NotificationService;
import com.bharat.springbootsocial.services.ServiceInt;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.util.Map;
import java.util.UUID;

@Controller
@AllArgsConstructor
public class NotificationWebSocketController {
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private ServiceInt userService;
    
    // Helper method to ensure JWT token has Bearer prefix
    private String ensureBearerPrefix(String jwt) {
        if (jwt != null && !jwt.startsWith("Bearer ")) {
            return "Bearer " + jwt;
        }
        return jwt;
    }
    
    // Handle notification subscription
    @MessageMapping("/notifications/subscribe")
    public void subscribeToNotifications(@Payload Map<String, Object> payload, SimpMessageHeaderAccessor headerAccessor) {
        try {
            String jwt = (String) payload.get("token");
            System.out.println("Received subscription request with token: " + (jwt != null ? "Present" : "Missing"));
            if (jwt != null) {
                User user = userService.getUserFromToken(ensureBearerPrefix(jwt));
                // User is now subscribed to their notification channel
                // The subscription is handled automatically by Spring WebSocket
                System.out.println("User " + user.getId() + " (" + user.getFname() + " " + user.getLname() + ") subscribed to notifications");
                System.out.println("User will receive notifications at: /user/" + user.getId() + "/queue/notifications");
            }
        } catch (Exception e) {
            System.err.println("Failed to subscribe user to notifications: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Handle notification read status update
    @MessageMapping("/notifications/mark-read")
    public void markNotificationAsRead(@Payload Map<String, Object> payload, SimpMessageHeaderAccessor headerAccessor) {
        try {
            String jwt = (String) payload.get("token");
            UUID notificationId = UUID.fromString(payload.get("notificationId").toString());
            
            if (jwt != null && notificationId != null) {
                User user = userService.getUserFromToken(ensureBearerPrefix(jwt));
                notificationService.markNotificationAsRead(notificationId, user);
            }
        } catch (Exception e) {
            System.err.println("Failed to mark notification as read: " + e.getMessage());
        }
    }
    
    // Handle mark all notifications as read
    @MessageMapping("/notifications/mark-all-read")
    public void markAllNotificationsAsRead(@Payload Map<String, Object> payload, SimpMessageHeaderAccessor headerAccessor) {
        try {
            String jwt = (String) payload.get("token");
            
            if (jwt != null) {
                User user = userService.getUserFromToken(ensureBearerPrefix(jwt));
                notificationService.markAllNotificationsAsRead(user.getId());
            }
        } catch (Exception e) {
            System.err.println("Failed to mark all notifications as read: " + e.getMessage());
        }
    }
}
