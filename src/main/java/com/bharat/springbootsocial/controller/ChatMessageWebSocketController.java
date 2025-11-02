package com.bharat.springbootsocial.controller;

import com.bharat.springbootsocial.entity.Chat;
import com.bharat.springbootsocial.entity.User;
import com.bharat.springbootsocial.request.EnhancedMessageRequest;
import com.bharat.springbootsocial.response.EnhancedMessageResponse;
import com.bharat.springbootsocial.services.ChatService;
import com.bharat.springbootsocial.services.ServiceInt;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.UUID;

@Controller
@AllArgsConstructor
public class ChatMessageWebSocketController {
    
    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;
    
    @Autowired
    private ChatService chatService;
    
    @Autowired
    private ServiceInt userService;
    
    /**
     * Handle direct chat message sending via WebSocket
     * Endpoint: /app/chat/{chatId}/send
     */
    @MessageMapping("/chat/{chatId}/send")
    public void sendMessage(@DestinationVariable UUID chatId, 
                           @Payload EnhancedMessageRequest request,
                           SimpMessageHeaderAccessor headerAccessor) {
        System.out.println("=== DIRECT CHAT WEBSOCKET REQUEST RECEIVED ===");
        System.out.println("Chat ID: " + chatId);
        System.out.println("Request: " + request);
        System.out.println("Request content: " + (request != null ? request.getContent() : "NULL"));
        System.out.println("Request action: " + (request != null ? request.getAction() : "NULL"));
        System.out.println("HeaderAccessor: " + (headerAccessor != null ? "NOT NULL" : "NULL"));
        
        try {
            // Extract JWT token from headers
            System.out.println("Extracting JWT token from headers...");
            String jwt = getJwtFromHeaders(headerAccessor);
            if (jwt == null || jwt.isEmpty()) {
                System.err.println("❌ JWT token is null or empty");
                throw new IllegalArgumentException("JWT token is required");
            }
            System.out.println("✓ JWT token extracted successfully");
            
            // Get sender from token
            System.out.println("Getting user from token...");
            User sender = userService.getUserFromToken(jwt);
            if (sender == null) {
                System.err.println("❌ User is null after token validation");
                throw new IllegalArgumentException("Invalid user token");
            }
            
            System.out.println("✓ User authenticated: " + sender.getFname() + " " + sender.getLname() + " (ID: " + sender.getId() + ")");
            
            // Validate request
            if (request == null) {
                System.err.println("❌ Request is null");
                throw new IllegalArgumentException("Request cannot be null");
            }
            
            // Ensure chatId is set in request
            if (request.getChatId() == null) {
                request.setChatId(chatId);
                System.out.println("✓ Set chatId in request: " + chatId);
            }
            
            EnhancedMessageResponse response;
            
            // Handle different actions
            String action = request.getAction();
            System.out.println("Processing action: " + action);
            
            if (action == null || action.isEmpty() || action.equals("send")) {
                // CREATE NEW MESSAGE - Save to database
                System.out.println("=== CREATING AND SAVING MESSAGE TO DATABASE ===");
                
                // Validate content
                if (request.getContent() == null || request.getContent().trim().isEmpty()) {
                    System.err.println("❌ Message content is null or empty");
                    throw new IllegalArgumentException("Message content cannot be empty");
                }
                
                System.out.println("Message content validated: " + request.getContent());
                System.out.println("Calling chatService.sendMessage() to save to database...");
                
                // Save message via service (this saves to database)
                try {
                    response = chatService.sendMessage(request, sender);
                    
                    if (response == null) {
                        System.err.println("❌ Response is NULL after chatService.sendMessage()");
                        throw new RuntimeException("Failed to save message to database: response is null");
                    }
                    
                    if (response.getId() == null) {
                        System.err.println("❌ Response ID is NULL after chatService.sendMessage()");
                        throw new RuntimeException("Failed to save message to database: message ID is null");
                    }
                    
                    System.out.println("✓✓✓ MESSAGE SAVED TO DATABASE SUCCESSFULLY ✓✓✓");
                    System.out.println("  Message ID: " + response.getId());
                    System.out.println("  Message content: " + response.getContent());
                    System.out.println("  Message chatId: " + response.getChatId());
                    System.out.println("  Message userId: " + response.getUserId());
                    System.out.println("  Message timestamp: " + response.getTimestamp());
                    
                } catch (Exception e) {
                    System.err.println("❌❌❌ ERROR DURING DATABASE SAVE ❌❌❌");
                    System.err.println("Exception type: " + e.getClass().getName());
                    System.err.println("Exception message: " + e.getMessage());
                    System.err.println("Stack trace:");
                    e.printStackTrace();
                    throw e;
                }
                
            } else if (action.equals("edit")) {
                // EDIT EXISTING MESSAGE
                if (request.getMessageId() == null) {
                    throw new IllegalArgumentException("Message ID is required for edit action");
                }
                System.out.println("Editing message ID: " + request.getMessageId());
                response = chatService.editMessage(request.getMessageId(), request.getContent(), sender);
                System.out.println("✓ Message edited successfully");
                
            } else if (action.equals("delete")) {
                // DELETE MESSAGE
                if (request.getMessageId() == null) {
                    throw new IllegalArgumentException("Message ID is required for delete action");
                }
                System.out.println("Deleting message ID: " + request.getMessageId());
                chatService.deleteMessage(request.getMessageId(), sender);
                
                // Create response for deleted message
                response = new EnhancedMessageResponse();
                response.setId(request.getMessageId());
                response.setChatId(chatId);
                response.setContent("This message was deleted");
                response.setIsDeleted(true);
                response.setDeletedAt(java.time.LocalDateTime.now());
                System.out.println("✓ Message deleted successfully");
                
            } else {
                throw new IllegalArgumentException("Invalid action: " + action);
            }
            
            // Broadcast to all chat participants (including sender for confirmation)
            System.out.println("Broadcasting message to participants...");
            broadcastToChatParticipants(chatId, response, sender.getId());
            System.out.println("✓ Message broadcast completed");
            
        } catch (Exception e) {
            System.err.println("❌ ERROR in sendMessage: " + e.getMessage());
            e.printStackTrace();
            
            // Send error to sender only
            try {
                UUID userId = getUserIdFromHeaders(headerAccessor);
                if (userId != null) {
                    EnhancedMessageResponse errorResponse = new EnhancedMessageResponse();
                    errorResponse.setChatId(chatId);
                    errorResponse.setContent("Error: " + e.getMessage());
                    
                    String errorDestination = "/user/" + userId + "/queue/chat-errors";
                    simpMessagingTemplate.convertAndSend(errorDestination, errorResponse);
                    System.out.println("Error sent to user: " + userId);
                }
            } catch (Exception ex) {
                System.err.println("Failed to send error response: " + ex.getMessage());
            }
        }
    }
    
    /**
     * Broadcast message to all chat participants
     * Uses topic-based broadcast (like group chat) for reliable delivery
     */
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    private void broadcastToChatParticipants(UUID chatId, EnhancedMessageResponse message, UUID senderId) {
        try {
            System.out.println("=== BROADCASTING CHAT MESSAGE ===");
            System.out.println("Chat ID: " + chatId);
            System.out.println("Sender ID: " + senderId);
            System.out.println("Message ID: " + message.getId());
            
            // Get chat with users eagerly loaded to avoid LazyInitializationException
            Chat chat = chatService.getChatByIdWithUsers(chatId);
            if (chat == null) {
                throw new RuntimeException("Chat not found: " + chatId);
            }
            
            List<User> participants = chat.getUsers();
            if (participants == null || participants.isEmpty()) {
                throw new RuntimeException("No participants found in chat: " + chatId);
            }
            
            System.out.println("Total participants: " + participants.size());
            
            // PRIMARY: Send to each participant via user queue (like group chat does)
            // Send to ALL participants including sender (for confirmation)
            for (User participant : participants) {
                UUID participantId = participant.getId();
                String queueDestination = "/user/" + participantId + "/queue/chat-messages";
                
                try {
                    simpMessagingTemplate.convertAndSend(queueDestination, message);
                    
                    if (participantId.equals(senderId)) {
                        System.out.println("✓ Confirmation sent to sender via queue: " + participantId + " (" + participant.getFname() + " " + participant.getLname() + ")");
                    } else {
                        System.out.println("✓ Message sent to recipient via queue: " + participantId + " (" + participant.getFname() + " " + participant.getLname() + ")");
                    }
                    System.out.println("  Queue Destination: " + queueDestination);
                } catch (Exception e) {
                    System.err.println("Failed to send to participant " + participantId + ": " + e.getMessage());
                }
            }
            
            // SECONDARY: Also broadcast to topic (for fallback, like group chat does)
            // This acts as fallback if user queues fail
            String topicDestination = "/chat/" + chatId;
            try {
                simpMessagingTemplate.convertAndSend(topicDestination, message);
                System.out.println("✓ Broadcast to topic: " + topicDestination + " (fallback)");
            } catch (Exception e) {
                System.err.println("Failed to broadcast to topic: " + e.getMessage());
            }
            
            System.out.println("✓ Broadcast completed for chat " + chatId);
            
        } catch (Exception e) {
            System.err.println("❌ ERROR in broadcastToChatParticipants: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to broadcast message: " + e.getMessage(), e);
        }
    }
    
    /**
     * Extract JWT token from WebSocket headers
     */
    private String getJwtFromHeaders(SimpMessageHeaderAccessor headerAccessor) {
        if (headerAccessor == null) {
            throw new IllegalArgumentException("HeaderAccessor is null");
        }
        
        List<String> authHeaders = headerAccessor.getNativeHeader("Authorization");
        if (authHeaders != null && !authHeaders.isEmpty()) {
            String authHeader = authHeaders.get(0);
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                return authHeader;
            }
        }
        throw new IllegalArgumentException("JWT token not found in headers");
    }
    
    /**
     * Get user ID from headers (for error responses)
     */
    private UUID getUserIdFromHeaders(SimpMessageHeaderAccessor headerAccessor) {
        try {
            String jwt = getJwtFromHeaders(headerAccessor);
            User user = userService.getUserFromToken(jwt);
            return user != null ? user.getId() : null;
        } catch (Exception e) {
            return null;
        }
    }
}
