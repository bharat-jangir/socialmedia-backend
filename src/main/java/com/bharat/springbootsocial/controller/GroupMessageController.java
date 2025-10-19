package com.bharat.springbootsocial.controller;

import com.bharat.springbootsocial.entity.GroupMessage;
import com.bharat.springbootsocial.entity.User;
import java.util.UUID;
import com.bharat.springbootsocial.response.ApiResponse;
import com.bharat.springbootsocial.response.GroupMessageResponse;
import com.bharat.springbootsocial.response.PaginatedResponse;
import com.bharat.springbootsocial.services.GroupMessageService;
import com.bharat.springbootsocial.services.ServiceInt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/groups/{groupId}/messages")
@CrossOrigin(origins = "http://localhost:5173")
public class GroupMessageController {
    
    @Autowired
    private GroupMessageService groupMessageService;
    
    @Autowired
    private ServiceInt userService;
    
    // Send text message
    @PostMapping
    public ResponseEntity<ApiResponse> sendMessage(
            @RequestHeader("Authorization") String jwt,
            @PathVariable UUID groupId,
            @RequestBody SendMessageRequest request) {
        try {
            User sender = userService.getUserFromToken(jwt);
            
            GroupMessage message = groupMessageService.sendMessage(
                groupId,
                sender,
                request.getContent(),
                request.getMessageType()
            );
            
            return new ResponseEntity<>(
                new ApiResponse("Message sent successfully", true, message),
                HttpStatus.CREATED
            );
            
        } catch (Exception e) {
            return new ResponseEntity<>(
                new ApiResponse("Failed to send message: " + e.getMessage(), false),
                HttpStatus.BAD_REQUEST
            );
        }
    }
    
    // Send media message
    @PostMapping("/media")
    public ResponseEntity<ApiResponse> sendMediaMessage(
            @RequestHeader("Authorization") String jwt,
            @PathVariable UUID groupId,
            @RequestBody SendMediaMessageRequest request) {
        try {
            User sender = userService.getUserFromToken(jwt);
            
            GroupMessage message = groupMessageService.sendMessageWithMedia(
                groupId,
                sender,
                request.getContent(),
                request.getMediaUrl(),
                request.getMessageType()
            );
            
            return new ResponseEntity<>(
                new ApiResponse("Media message sent successfully", true, message),
                HttpStatus.CREATED
            );
            
        } catch (Exception e) {
            return new ResponseEntity<>(
                new ApiResponse("Failed to send media message: " + e.getMessage(), false),
                HttpStatus.BAD_REQUEST
            );
        }
    }
    
    // Send reply message
    @PostMapping("/reply")
    public ResponseEntity<ApiResponse> sendReplyMessage(
            @RequestHeader("Authorization") String jwt,
            @PathVariable UUID groupId,
            @RequestBody SendReplyMessageRequest request) {
        try {
            User sender = userService.getUserFromToken(jwt);
            
            GroupMessage message = groupMessageService.sendReplyMessage(
                groupId,
                sender,
                request.getContent(),
                request.getReplyToMessageId()
            );
            
            return new ResponseEntity<>(
                new ApiResponse("Reply sent successfully", true, message),
                HttpStatus.CREATED
            );
            
        } catch (Exception e) {
            return new ResponseEntity<>(
                new ApiResponse("Failed to send reply: " + e.getMessage(), false),
                HttpStatus.BAD_REQUEST
            );
        }
    }
    
    // Get group messages
    @GetMapping
    public ResponseEntity<ApiResponse> getGroupMessages(
            @RequestHeader("Authorization") String jwt,
            @PathVariable UUID groupId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            User user = userService.getUserFromToken(jwt);
            
            PaginatedResponse<GroupMessage> messages = groupMessageService.getGroupMessagesPaginated(groupId, page, size);
            
            // Convert to GroupMessageResponse with detailed reactions and read receipts
            PaginatedResponse<GroupMessageResponse> responseMessages = new PaginatedResponse<>(
                messages.getContent().stream()
                    .map(GroupMessageResponse::new)
                    .collect(java.util.stream.Collectors.toList()),
                messages.getPage(),
                messages.getSize(),
                messages.getTotalElements(),
                messages.getTotalPages(),
                messages.isHasNext(),
                messages.isHasPrevious(),
                messages.isFirst(),
                messages.isLast()
            );
            
            return new ResponseEntity<>(
                new ApiResponse("Messages retrieved successfully", true, responseMessages),
                HttpStatus.OK
            );
            
        } catch (Exception e) {
            return new ResponseEntity<>(
                new ApiResponse("Failed to get messages: " + e.getMessage(), false),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }
    
    // Get media messages
    @GetMapping("/media")
    public ResponseEntity<ApiResponse> getMediaMessages(
            @RequestHeader("Authorization") String jwt,
            @PathVariable UUID groupId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            User user = userService.getUserFromToken(jwt);
            
            PaginatedResponse<GroupMessage> messages = groupMessageService.getGroupMediaMessagesPaginated(groupId, page, size);
            
            return new ResponseEntity<>(
                new ApiResponse("Media messages retrieved successfully", true, messages),
                HttpStatus.OK
            );
            
        } catch (Exception e) {
            return new ResponseEntity<>(
                new ApiResponse("Failed to get media messages: " + e.getMessage(), false),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }
    
    // Search messages
    @GetMapping("/search")
    public ResponseEntity<ApiResponse> searchMessages(
            @RequestHeader("Authorization") String jwt,
            @PathVariable UUID groupId,
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            User user = userService.getUserFromToken(jwt);
            
            PaginatedResponse<GroupMessage> messages = groupMessageService.searchMessagesInGroupPaginated(groupId, query, page, size);
            
            return new ResponseEntity<>(
                new ApiResponse("Search completed successfully", true, messages),
                HttpStatus.OK
            );
            
        } catch (Exception e) {
            return new ResponseEntity<>(
                new ApiResponse("Failed to search messages: " + e.getMessage(), false),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }
    
    // Edit message
    @PutMapping("/{messageId}")
    public ResponseEntity<ApiResponse> editMessage(
            @RequestHeader("Authorization") String jwt,
            @PathVariable UUID groupId,
            @PathVariable UUID messageId,
            @RequestBody Map<String, String> request) {
        try {
            User user = userService.getUserFromToken(jwt);
            String newContent = request.get("content");
            
            if (newContent == null) {
                return new ResponseEntity<>(
                    new ApiResponse("Content is required", false),
                    HttpStatus.BAD_REQUEST
                );
            }
            
            GroupMessage message = groupMessageService.editMessage(messageId, user, newContent);
            
            return new ResponseEntity<>(
                new ApiResponse("Message edited successfully", true, message),
                HttpStatus.OK
            );
            
        } catch (Exception e) {
            return new ResponseEntity<>(
                new ApiResponse("Failed to edit message: " + e.getMessage(), false),
                HttpStatus.BAD_REQUEST
            );
        }
    }
    
    // Delete message
    @DeleteMapping("/{messageId}")
    public ResponseEntity<ApiResponse> deleteMessage(
            @RequestHeader("Authorization") String jwt,
            @PathVariable UUID groupId,
            @PathVariable UUID messageId) {
        try {
            User user = userService.getUserFromToken(jwt);
            
            groupMessageService.deleteMessage(messageId, user);
            
            return new ResponseEntity<>(
                new ApiResponse("Message deleted successfully", true),
                HttpStatus.OK
            );
            
        } catch (Exception e) {
            return new ResponseEntity<>(
                new ApiResponse("Failed to delete message: " + e.getMessage(), false),
                HttpStatus.BAD_REQUEST
            );
        }
    }
    
    // Add reaction
    @PostMapping("/{messageId}/reactions")
    public ResponseEntity<ApiResponse> addReaction(
            @RequestHeader("Authorization") String jwt,
            @PathVariable UUID groupId,
            @PathVariable UUID messageId,
            @RequestBody Map<String, String> request) {
        try {
            User user = userService.getUserFromToken(jwt);
            String emoji = request.get("emoji");
            
            if (emoji == null) {
                return new ResponseEntity<>(
                    new ApiResponse("Emoji is required", false),
                    HttpStatus.BAD_REQUEST
                );
            }
            
            GroupMessage message = groupMessageService.addReaction(messageId, user, emoji);
            
            return new ResponseEntity<>(
                new ApiResponse("Reaction added successfully", true, message),
                HttpStatus.OK
            );
            
        } catch (Exception e) {
            return new ResponseEntity<>(
                new ApiResponse("Failed to add reaction: " + e.getMessage(), false),
                HttpStatus.BAD_REQUEST
            );
        }
    }
    
    // Remove reaction
    @DeleteMapping("/{messageId}/reactions")
    public ResponseEntity<ApiResponse> removeReaction(
            @RequestHeader("Authorization") String jwt,
            @PathVariable UUID groupId,
            @PathVariable UUID messageId,
            @RequestBody Map<String, String> request) {
        try {
            User user = userService.getUserFromToken(jwt);
            String emoji = request.get("emoji");
            
            if (emoji == null) {
                return new ResponseEntity<>(
                    new ApiResponse("Emoji is required", false),
                    HttpStatus.BAD_REQUEST
                );
            }
            
            groupMessageService.removeReaction(messageId, user, emoji);
            
            return new ResponseEntity<>(
                new ApiResponse("Reaction removed successfully", true),
                HttpStatus.OK
            );
            
        } catch (Exception e) {
            return new ResponseEntity<>(
                new ApiResponse("Failed to remove reaction: " + e.getMessage(), false),
                HttpStatus.BAD_REQUEST
            );
        }
    }
    
    // Mark messages as read
    @PostMapping("/read")
    public ResponseEntity<ApiResponse> markMessagesAsRead(
            @RequestHeader("Authorization") String jwt,
            @PathVariable UUID groupId) {
        try {
            User user = userService.getUserFromToken(jwt);
            
            groupMessageService.markAllMessagesAsRead(groupId, user);
            
            return new ResponseEntity<>(
                new ApiResponse("Messages marked as read successfully", true),
                HttpStatus.OK
            );
            
        } catch (Exception e) {
            return new ResponseEntity<>(
                new ApiResponse("Failed to mark messages as read: " + e.getMessage(), false),
                HttpStatus.BAD_REQUEST
            );
        }
    }
    
    // Mark specific message as read
    @PostMapping("/{messageId}/read")
    public ResponseEntity<ApiResponse> markMessageAsRead(
            @RequestHeader("Authorization") String jwt,
            @PathVariable UUID groupId,
            @PathVariable UUID messageId) {
        try {
            User user = userService.getUserFromToken(jwt);
            
            groupMessageService.markMessageAsRead(messageId, user);
            
            return new ResponseEntity<>(
                new ApiResponse("Message marked as read successfully", true),
                HttpStatus.OK
            );
            
        } catch (Exception e) {
            return new ResponseEntity<>(
                new ApiResponse("Failed to mark message as read: " + e.getMessage(), false),
                HttpStatus.BAD_REQUEST
            );
        }
    }
    
    // Get unread message count
    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse> getUnreadMessageCount(
            @RequestHeader("Authorization") String jwt,
            @PathVariable UUID groupId) {
        try {
            User user = userService.getUserFromToken(jwt);
            
            int unreadCount = groupMessageService.getUnreadMessageCount(groupId, user);
            
            return new ResponseEntity<>(
                new ApiResponse("Unread count retrieved successfully", true, Map.of("unreadCount", unreadCount)),
                HttpStatus.OK
            );
            
        } catch (Exception e) {
            return new ResponseEntity<>(
                new ApiResponse("Failed to get unread count: " + e.getMessage(), false),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }
    
    // Get message by ID
    @GetMapping("/{messageId}")
    public ResponseEntity<ApiResponse> getMessageById(
            @RequestHeader("Authorization") String jwt,
            @PathVariable UUID groupId,
            @PathVariable UUID messageId) {
        try {
            User user = userService.getUserFromToken(jwt);
            
            GroupMessage message = groupMessageService.getMessageById(messageId);
            
            return new ResponseEntity<>(
                new ApiResponse("Message retrieved successfully", true, message),
                HttpStatus.OK
            );
            
        } catch (Exception e) {
            return new ResponseEntity<>(
                new ApiResponse("Failed to get message: " + e.getMessage(), false),
                HttpStatus.NOT_FOUND
            );
        }
    }
    
    // DTOs
    public static class SendMessageRequest {
        private String content;
        private GroupMessage.MessageType messageType;
        
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        
        public GroupMessage.MessageType getMessageType() { return messageType; }
        public void setMessageType(GroupMessage.MessageType messageType) { this.messageType = messageType; }
    }
    
    public static class SendMediaMessageRequest {
        private String content;
        private String mediaUrl;
        private GroupMessage.MessageType messageType;
        
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        
        public String getMediaUrl() { return mediaUrl; }
        public void setMediaUrl(String mediaUrl) { this.mediaUrl = mediaUrl; }
        
        public GroupMessage.MessageType getMessageType() { return messageType; }
        public void setMessageType(GroupMessage.MessageType messageType) { this.messageType = messageType; }
    }
    
    public static class SendReplyMessageRequest {
        private String content;
        private UUID replyToMessageId;
        
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        
        public UUID getReplyToMessageId() { return replyToMessageId; }
        public void setReplyToMessageId(UUID replyToMessageId) { this.replyToMessageId = replyToMessageId; }
    }
}

