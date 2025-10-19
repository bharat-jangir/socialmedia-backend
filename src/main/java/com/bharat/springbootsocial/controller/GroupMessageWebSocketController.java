package com.bharat.springbootsocial.controller;

import com.bharat.springbootsocial.entity.GroupMessage;
import com.bharat.springbootsocial.entity.GroupMember;
import com.bharat.springbootsocial.entity.User;
import com.bharat.springbootsocial.request.GroupMessageWebSocketRequest;
import com.bharat.springbootsocial.response.GroupMessageResponse;
import com.bharat.springbootsocial.response.GroupMessageWebSocketResponse;
import com.bharat.springbootsocial.services.GroupMessageService;
import com.bharat.springbootsocial.services.GroupService;
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
public class GroupMessageWebSocketController {
    
    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;
    
    @Autowired
    private GroupMessageService groupMessageService;
    
    @Autowired
    private ServiceInt userService;
    
    @Autowired
    private GroupService groupService;
    
    // Send message to group
    @MessageMapping("/group/{groupId}/send")
    public void sendMessage(@DestinationVariable UUID groupId, 
                           @Payload GroupMessageWebSocketRequest request,
                           SimpMessageHeaderAccessor headerAccessor) {
        try {
            // Get user from JWT token in headers
            String jwt = getJwtFromHeaders(headerAccessor);
            User sender = userService.getUserFromToken(jwt);
            
            GroupMessage message;
            
            if (request.getAction().equals("send")) {
                if (request.getMediaUrl() != null) {
                    // Send media message
                    message = groupMessageService.sendMessageWithMedia(
                        groupId, sender, request.getContent(), 
                        request.getMediaUrl(), request.getMessageType()
                    );
                } else if (request.getReplyToMessageId() != null) {
                    // Send reply message
                    message = groupMessageService.sendReplyMessage(
                        groupId, sender, request.getContent(), 
                        request.getReplyToMessageId()
                    );
                } else {
                    // Send text message
                    message = groupMessageService.sendMessage(
                        groupId, sender, request.getContent(), 
                        request.getMessageType()
                    );
                }
                
                // Convert to response and broadcast to all group members except sender
                GroupMessageResponse response = new GroupMessageResponse(message);
                broadcastToGroupExceptSender(groupId, response, sender.getId());
                
            } else if (request.getAction().equals("edit")) {
                // Edit message
                message = groupMessageService.editMessage(
                    request.getMessageId(), sender, request.getContent()
                );
                
                GroupMessageResponse response = new GroupMessageResponse(message);
                broadcastToGroupExceptSender(groupId, response, sender.getId());
                
            } else if (request.getAction().equals("delete")) {
                // Delete message
                groupMessageService.deleteMessage(request.getMessageId(), sender);
                
                // Create a simple response for deleted message
                GroupMessageResponse response = new GroupMessageResponse();
                response.setId(request.getMessageId());
                response.setContent("This message was deleted");
                response.setIsDeleted(true);
                response.setDeletedAt(java.time.LocalDateTime.now());
                
                broadcastToGroupExceptSender(groupId, response, sender.getId());
            }
            
        } catch (Exception e) {
            // Send error response
            GroupMessageWebSocketResponse errorResponse = new GroupMessageWebSocketResponse();
            errorResponse.setGroupId(groupId);
            errorResponse.setAction("error");
            errorResponse.setContent("Error: " + e.getMessage());
            
            simpMessagingTemplate.convertAndSend("/user/" + getUserIdFromHeaders(headerAccessor) + "/queue/errors", errorResponse);
        }
    }
    
    // Handle message reactions
    @MessageMapping("/group/{groupId}/react")
    public void handleReaction(@DestinationVariable UUID groupId,
                              @Payload GroupMessageWebSocketRequest request,
                              SimpMessageHeaderAccessor headerAccessor) {
        try {
            System.out.println("=== REACTION WEBSOCKET REQUEST ===");
            System.out.println("Group ID: " + groupId);
            System.out.println("Request: " + request);
            System.out.println("Action: " + request.getAction());
            System.out.println("Message ID: " + request.getMessageId());
            System.out.println("Reaction: " + request.getReaction());
            
            String jwt = getJwtFromHeaders(headerAccessor);
            User user = userService.getUserFromToken(jwt);
            System.out.println("User: " + user.getFname() + " " + user.getLname() + " (ID: " + user.getId() + ")");
            
            if (request.getAction().equals("react")) {
                System.out.println("Adding reaction...");
                // Add reaction
                groupMessageService.addReaction(
                    request.getMessageId(), user, request.getReaction()
                );
                System.out.println("Reaction added successfully");
                
                // Get updated message with reactions
                GroupMessage updatedMessage = groupMessageService.getMessageById(request.getMessageId());
                System.out.println("Updated message reactions count: " + (updatedMessage.getReactions() != null ? updatedMessage.getReactions().size() : 0));
                
                GroupMessageResponse response = new GroupMessageResponse(updatedMessage);
                System.out.println("Response reaction counts: " + response.getReactionCounts());
                System.out.println("Response total reaction count: " + response.getTotalReactionCount());
                
                System.out.println("Broadcasting to: /group/" + groupId + " (excluding sender)");
                broadcastToGroupExceptSender(groupId, response, user.getId());
                System.out.println("Broadcast sent successfully");
                
            } else if (request.getAction().equals("remove_reaction")) {
                // Remove reaction
                groupMessageService.removeReaction(
                    request.getMessageId(), user, request.getReaction()
                );
                
                // Get updated message with reactions
                GroupMessage updatedMessage = groupMessageService.getMessageById(request.getMessageId());
                GroupMessageResponse response = new GroupMessageResponse(updatedMessage);
                
                broadcastToGroupExceptSender(groupId, response, user.getId());
                
            } else if (request.getAction().equals("remove_all_reactions")) {
                // Remove all reactions by user
                groupMessageService.removeAllReactions(
                    request.getMessageId(), user
                );
                
                // Get updated message with reactions
                GroupMessage updatedMessage = groupMessageService.getMessageById(request.getMessageId());
                GroupMessageResponse response = new GroupMessageResponse(updatedMessage);
                
                broadcastToGroupExceptSender(groupId, response, user.getId());
            }
            
        } catch (Exception e) {
            GroupMessageWebSocketResponse errorResponse = new GroupMessageWebSocketResponse();
            errorResponse.setGroupId(groupId);
            errorResponse.setAction("error");
            errorResponse.setContent("Error: " + e.getMessage());
            
            simpMessagingTemplate.convertAndSend("/user/" + getUserIdFromHeaders(headerAccessor) + "/queue/errors", errorResponse);
        }
    }
    
    // Handle typing indicators
    @MessageMapping("/group/{groupId}/typing")
    public void handleTyping(@DestinationVariable UUID groupId,
                            @Payload GroupMessageWebSocketRequest request,
                            SimpMessageHeaderAccessor headerAccessor) {
        try {
            String jwt = getJwtFromHeaders(headerAccessor);
            User user = userService.getUserFromToken(jwt);
            
            GroupMessageWebSocketResponse response = new GroupMessageWebSocketResponse();
            response.setGroupId(groupId);
            response.setAction(request.getAction()); // "typing" or "stop_typing"
            response.setSender(convertToUserResponse(user));
            
            // Broadcast typing indicator to all group members except sender
            broadcastToGroupExceptSender(groupId, response, user.getId());
            
            System.out.println("DEBUG: Sent typing indicator to group " + groupId + " for user " + user.getId());
            
        } catch (Exception e) {
            // Handle error silently for typing indicators
            System.err.println("DEBUG: Error in handleTyping: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    
    // Handle message read receipts
    @MessageMapping("/group/{groupId}/read")
    public void handleReadReceipt(@DestinationVariable UUID groupId,
                                 @Payload GroupMessageWebSocketRequest request,
                                 SimpMessageHeaderAccessor headerAccessor) {
        try {
            String jwt = getJwtFromHeaders(headerAccessor);
            User user = userService.getUserFromToken(jwt);
            
            // Mark message as read
            groupMessageService.markMessageAsRead(request.getMessageId(), user);
            
            GroupMessageWebSocketResponse response = new GroupMessageWebSocketResponse();
            response.setMessageId(request.getMessageId());
            response.setGroupId(groupId);
            response.setAction("message_read");
            response.setSender(convertToUserResponse(user));
            
            broadcastToGroupExceptSender(groupId, response, user.getId());
            
        } catch (Exception e) {
            // Handle error silently for read receipts
        }
    }
    
    // Helper methods
    private String getJwtFromHeaders(SimpMessageHeaderAccessor headerAccessor) {
        List<String> authHeaders = headerAccessor.getNativeHeader("Authorization");
        if (authHeaders != null && !authHeaders.isEmpty()) {
            String authHeader = authHeaders.get(0);
            if (authHeader.startsWith("Bearer ")) {
                return authHeader;
            }
        }
        throw new IllegalArgumentException("JWT token not found in headers");
    }
    
    private UUID getUserIdFromHeaders(SimpMessageHeaderAccessor headerAccessor) {
        try {
            String jwt = getJwtFromHeaders(headerAccessor);
            User user = userService.getUserFromToken(jwt);
            return user.getId();
        } catch (Exception e) {
            return null;
        }
    }
    
    
    private GroupMessageWebSocketResponse.UserResponse convertToUserResponse(User user) {
        GroupMessageWebSocketResponse.UserResponse userResponse = new GroupMessageWebSocketResponse.UserResponse();
        userResponse.setId(user.getId());
        userResponse.setFirstName(user.getFname());
        userResponse.setLastName(user.getLname());
        userResponse.setEmail(user.getEmail());
        userResponse.setProfileImage(user.getProfileImage());
        return userResponse;
    }
    
    /**
     * Broadcast message to all group members except the sender
     */
    private void broadcastToGroupExceptSender(UUID groupId, Object message, UUID senderId) {
        try {
            // Get all group members
            List<GroupMember> groupMembers = groupService.getGroupMembers(groupId);
            
            System.out.println("DEBUG: Broadcasting to group " + groupId + " (excluding sender " + senderId + ")");
            System.out.println("DEBUG: Total group members: " + groupMembers.size());
            
            // Send to each member except sender
            for (GroupMember groupMember : groupMembers) {
                User member = groupMember.getUser();
                if (!member.getId().equals(senderId)) {
                    String destination = "/user/" + member.getId() + "/queue/group-messages";
                    simpMessagingTemplate.convertAndSend(destination, message);
                    System.out.println("DEBUG: Sent to user " + member.getId() + " (" + member.getFname() + " " + member.getLname() + ")");
                } else {
                    System.out.println("DEBUG: Excluded sender " + member.getId() + " (" + member.getFname() + " " + member.getLname() + ")");
                }
            }
            
            System.out.println("DEBUG: Broadcast completed successfully");
            
        } catch (Exception e) {
            System.err.println("DEBUG: Error in broadcastToGroupExceptSender: " + e.getMessage());
            e.printStackTrace();
            
            // Fallback to group broadcast if individual messaging fails
            simpMessagingTemplate.convertAndSend("/group/" + groupId, message);
        }
    }
}
