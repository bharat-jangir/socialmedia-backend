package com.bharat.springbootsocial.controller;

import com.bharat.springbootsocial.entity.Notification;
import com.bharat.springbootsocial.entity.User;
import java.util.UUID;
import com.bharat.springbootsocial.response.ApiResponse;
import com.bharat.springbootsocial.services.NotificationService;
import com.bharat.springbootsocial.services.ServiceInt;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@AllArgsConstructor
@CrossOrigin(origins = "*")
public class NotificationController {
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private ServiceInt userService;
    
    // Get all notifications for current user
    @GetMapping
    public ResponseEntity<List<Notification>> getNotifications(
            @RequestHeader("Authorization") String jwt) {
        try {
            User reqUser = userService.getUserFromToken(jwt);
            List<Notification> notifications = notificationService.getNotificationsByUserId(reqUser.getId());
            return new ResponseEntity<>(notifications, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }
    
    // Get paginated notifications for current user
    @GetMapping("/paginated")
    public ResponseEntity<Page<Notification>> getNotificationsPaginated(
            @RequestHeader("Authorization") String jwt,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            User reqUser = userService.getUserFromToken(jwt);
            Pageable pageable = PageRequest.of(page, size);
            Page<Notification> notifications = notificationService.getNotificationsByUserIdPaginated(reqUser.getId(), pageable);
            return new ResponseEntity<>(notifications, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }
    
    // Get unread notifications for current user
    @GetMapping("/unread")
    public ResponseEntity<List<Notification>> getUnreadNotifications(
            @RequestHeader("Authorization") String jwt) {
        try {
            User reqUser = userService.getUserFromToken(jwt);
            List<Notification> notifications = notificationService.getUnreadNotificationsByUserId(reqUser.getId());
            return new ResponseEntity<>(notifications, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }
    
    // Get unread notification count
    @GetMapping("/unread/count")
    public ResponseEntity<ApiResponse> getUnreadNotificationCount(
            @RequestHeader("Authorization") String jwt) {
        try {
            User reqUser = userService.getUserFromToken(jwt);
            Integer count = notificationService.getUnreadNotificationCount(reqUser.getId());
            
            ApiResponse response = new ApiResponse();
            response.setMessage("Unread notification count retrieved successfully");
            response.setStatus(true);
            response.setData(new Object() {
                public final Integer unreadCount = count;
            });
            
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }
    
    // Mark notification as read
    @PutMapping("/{notificationId}/read")
    public ResponseEntity<ApiResponse> markNotificationAsRead(
            @PathVariable UUID notificationId,
            @RequestHeader("Authorization") String jwt) {
        try {
            User reqUser = userService.getUserFromToken(jwt);
            notificationService.markNotificationAsRead(notificationId, reqUser);
            
            ApiResponse response = new ApiResponse("Notification marked as read", true);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiResponse response = new ApiResponse("Failed to mark notification as read: " + e.getMessage(), false);
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }
    
    // Mark all notifications as read
    @PutMapping("/read-all")
    public ResponseEntity<ApiResponse> markAllNotificationsAsRead(
            @RequestHeader("Authorization") String jwt) {
        try {
            User reqUser = userService.getUserFromToken(jwt);
            notificationService.markAllNotificationsAsRead(reqUser.getId());
            
            ApiResponse response = new ApiResponse("All notifications marked as read", true);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiResponse response = new ApiResponse("Failed to mark all notifications as read: " + e.getMessage(), false);
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }
    
    // Delete notification
    @DeleteMapping("/{notificationId}")
    public ResponseEntity<ApiResponse> deleteNotification(
            @PathVariable UUID notificationId,
            @RequestHeader("Authorization") String jwt) {
        try {
            User reqUser = userService.getUserFromToken(jwt);
            notificationService.deleteNotification(notificationId, reqUser);
            
            ApiResponse response = new ApiResponse("Notification deleted successfully", true);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiResponse response = new ApiResponse("Failed to delete notification: " + e.getMessage(), false);
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }
    
    // Delete all notifications for current user
    @DeleteMapping("/all")
    public ResponseEntity<ApiResponse> deleteAllNotifications(
            @RequestHeader("Authorization") String jwt) {
        try {
            User reqUser = userService.getUserFromToken(jwt);
            notificationService.deleteAllNotifications(reqUser.getId());
            
            ApiResponse response = new ApiResponse("All notifications deleted successfully", true);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiResponse response = new ApiResponse("Failed to delete all notifications: " + e.getMessage(), false);
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }
    
    // Get notifications by type
    @GetMapping("/type/{type}")
    public ResponseEntity<List<Notification>> getNotificationsByType(
            @PathVariable String type,
            @RequestHeader("Authorization") String jwt) {
        try {
            User reqUser = userService.getUserFromToken(jwt);
            Notification.NotificationType notificationType = Notification.NotificationType.valueOf(type.toUpperCase());
            List<Notification> notifications = notificationService.getNotificationsByType(reqUser.getId(), notificationType);
            return new ResponseEntity<>(notifications, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}
