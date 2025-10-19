package com.bharat.springbootsocial.controller;

import com.bharat.springbootsocial.entity.User;
import com.bharat.springbootsocial.services.ServiceInt;
import com.bharat.springbootsocial.services.WebRTCSignalingService;
import com.bharat.springbootsocial.services.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Controller
public class WebRTCSignalingController {
    
    private static final Logger logger = LoggerFactory.getLogger(WebRTCSignalingController.class);
    
    @Autowired
    private WebRTCSignalingService signalingService;
    
    @Autowired
    private ServiceInt userService;
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    // Helper method to ensure JWT token has Bearer prefix
    private String ensureBearerPrefix(String jwt) {
        if (jwt != null && !jwt.startsWith("Bearer ")) {
            return "Bearer " + jwt;
        }
        return jwt;
    }
    
    // Helper method to send error responses
    private void sendErrorResponse(SimpMessageHeaderAccessor headerAccessor, String errorMessage) {
        try {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("type", "error");
            errorResponse.put("message", errorMessage);
            errorResponse.put("timestamp", System.currentTimeMillis());
            
            String sessionId = headerAccessor.getSessionId();
            if (sessionId != null) {
                messagingTemplate.convertAndSend("/user/queue/errors", errorResponse);
            }
        } catch (Exception e) {
            logger.error("Failed to send error response: {}", e.getMessage());
        }
    }
    
    // Handle WebRTC offer
    @MessageMapping("/calls/offer")
    public void handleOffer(@Payload Map<String, Object> payload, SimpMessageHeaderAccessor headerAccessor) {
        try {
            logger.info("=== 📨 INCOMING OFFER MESSAGE ===");
            logger.info("📥 Received offer message: {}", payload);
            
            // Validate required fields
            String jwt = (String) payload.get("token");
            if (jwt == null) {
                logger.error("❌ No JWT token provided for offer");
                sendErrorResponse(headerAccessor, "No JWT token provided");
                return;
            }
            
            String roomId = (String) payload.get("roomId");
            if (roomId == null || roomId.trim().isEmpty()) {
                logger.error("❌ No roomId provided for offer");
                sendErrorResponse(headerAccessor, "No roomId provided");
                return;
            }
            
            Object toUserIdObj = payload.get("toUserId");
            if (toUserIdObj == null) {
                logger.error("❌ No toUserId provided for offer");
                sendErrorResponse(headerAccessor, "No toUserId provided");
                return;
            }
            
            // Handle both direct offer and offer in data field
            Object offerObj = payload.get("data");
            if (offerObj == null) {
                offerObj = payload.get("offer");
            }
            if (offerObj == null) {
                logger.error("❌ No offer data provided");
                sendErrorResponse(headerAccessor, "No offer data provided");
                return;
            }
            
            if (!(offerObj instanceof Map)) {
                logger.error("❌ Invalid offer data format");
                sendErrorResponse(headerAccessor, "Invalid offer data format");
                return;
            }
            
            @SuppressWarnings("unchecked")
            Map<String, Object> offer = (Map<String, Object>) offerObj;
            
            // Authenticate user
            User fromUser = userService.getUserFromToken(ensureBearerPrefix(jwt));
            if (fromUser == null) {
                logger.error("❌ Invalid JWT token for offer");
                sendErrorResponse(headerAccessor, "Invalid JWT token");
                return;
            }
            
            UUID toUserId = UUID.fromString(toUserIdObj.toString());
            User toUser = userService.getUserById(toUserId);
            if (toUser == null) {
                logger.error("❌ Target user not found: {}", toUserId);
                sendErrorResponse(headerAccessor, "Target user not found");
                return;
            }
            
            logger.info("👤 From User: {} ({})", fromUser.getId(), fromUser.getFname() + " " + fromUser.getLname());
            logger.info("👤 To User: {} ({})", toUser.getId(), toUser.getFname() + " " + toUser.getLname());
            logger.info("🏠 Room ID: {}", roomId);
            logger.info("📋 Offer Type: {}", offer.get("type"));
            
            signalingService.sendOffer(roomId, fromUser, toUser, offer);
            logger.info("✅ WebRTC offer handled successfully!");
            
        } catch (IllegalArgumentException e) {
            logger.error("❌ Invalid UUID format: {}", e.getMessage());
            sendErrorResponse(headerAccessor, "Invalid user ID format");
        } catch (Exception e) {
            logger.error("❌ Failed to handle WebRTC offer: {}", e.getMessage(), e);
            sendErrorResponse(headerAccessor, "Failed to handle offer: " + e.getMessage());
        }
    }
    
    // Handle WebRTC answer
    @MessageMapping("/calls/answer")
    public void handleAnswer(@Payload Map<String, Object> payload, SimpMessageHeaderAccessor headerAccessor) {
        try {
            logger.info("=== 📨 INCOMING ANSWER MESSAGE ===");
            logger.info("📥 Received answer message: {}", payload);
            
            // Validate required fields
            String jwt = (String) payload.get("token");
            if (jwt == null) {
                logger.error("❌ No JWT token provided for answer");
                sendErrorResponse(headerAccessor, "No JWT token provided");
                return;
            }
            
            String roomId = (String) payload.get("roomId");
            if (roomId == null || roomId.trim().isEmpty()) {
                logger.error("❌ No roomId provided for answer");
                sendErrorResponse(headerAccessor, "No roomId provided");
                return;
            }
            
            Object answerObj = payload.get("answer");
            if (answerObj == null) {
                logger.error("❌ No answer data provided");
                sendErrorResponse(headerAccessor, "No answer data provided");
                return;
            }
            
            if (!(answerObj instanceof Map)) {
                logger.error("❌ Invalid answer data format");
                sendErrorResponse(headerAccessor, "Invalid answer data format");
                return;
            }
            
            @SuppressWarnings("unchecked")
            Map<String, Object> answer = (Map<String, Object>) answerObj;
            
            // Authenticate user
            User fromUser = userService.getUserFromToken(ensureBearerPrefix(jwt));
            if (fromUser == null) {
                logger.error("❌ Invalid JWT token for answer");
                sendErrorResponse(headerAccessor, "Invalid JWT token");
                return;
            }
            
            logger.info("👤 From User: {} ({})", fromUser.getId(), fromUser.getFname() + " " + fromUser.getLname());
            logger.info("🏠 Room ID: {}", roomId);
            logger.info("📋 Answer Type: {}", answer.get("type"));
            
            // Handle missing toUserId - broadcast to all participants in room
            Object toUserIdObj = payload.get("toUserId");
            if (toUserIdObj != null) {
                // Send to specific user
                try {
                    UUID toUserId = UUID.fromString(toUserIdObj.toString());
                    User toUser = userService.getUserById(toUserId);
                    if (toUser == null) {
                        logger.error("❌ Target user not found: {}", toUserId);
                        sendErrorResponse(headerAccessor, "Target user not found");
                        return;
                    }
                    logger.info("👤 Target User: {} ({})", toUser.getId(), toUser.getFname() + " " + toUser.getLname());
                    signalingService.sendAnswer(roomId, fromUser, toUser, answer);
                } catch (IllegalArgumentException e) {
                    logger.error("❌ Invalid UUID format: {}", e.getMessage());
                    sendErrorResponse(headerAccessor, "Invalid user ID format");
                    return;
                }
            } else {
                // Broadcast to all participants in room (for peer-to-peer calls)
                logger.info("📡 Broadcasting answer to all participants in room");
                signalingService.broadcastAnswer(roomId, fromUser, answer);
            }
            logger.info("✅ WebRTC answer handled successfully!");
            
        } catch (Exception e) {
            logger.error("❌ Failed to handle WebRTC answer: {}", e.getMessage(), e);
            sendErrorResponse(headerAccessor, "Failed to handle answer: " + e.getMessage());
        }
    }
    
    // Handle ICE candidate
    @MessageMapping("/calls/ice-candidate")
    public void handleIceCandidate(@Payload Map<String, Object> payload, SimpMessageHeaderAccessor headerAccessor) {
        try {
            logger.info("=== 📨 INCOMING ICE CANDIDATE MESSAGE ===");
            logger.info("📥 Received ICE candidate message: {}", payload);
            
            // Validate required fields
            String jwt = (String) payload.get("token");
            if (jwt == null) {
                logger.error("❌ No JWT token provided for ICE candidate");
                sendErrorResponse(headerAccessor, "No JWT token provided");
                return;
            }
            
            String roomId = (String) payload.get("roomId");
            if (roomId == null || roomId.trim().isEmpty()) {
                logger.error("❌ No roomId provided for ICE candidate");
                sendErrorResponse(headerAccessor, "No roomId provided");
                return;
            }
            
            // Handle both direct candidate and candidate in data field
            Object candidateObj = payload.get("data");
            if (candidateObj == null) {
                candidateObj = payload.get("candidate");
            }
            if (candidateObj == null) {
                logger.error("❌ No candidate data provided");
                sendErrorResponse(headerAccessor, "No candidate data provided");
                return;
            }
            
            if (!(candidateObj instanceof Map)) {
                logger.error("❌ Invalid candidate data format");
                sendErrorResponse(headerAccessor, "Invalid candidate data format");
                return;
            }
            
            @SuppressWarnings("unchecked")
            Map<String, Object> candidate = (Map<String, Object>) candidateObj;
            
            // Authenticate user
            User fromUser = userService.getUserFromToken(ensureBearerPrefix(jwt));
            if (fromUser == null) {
                logger.error("❌ Invalid JWT token for ICE candidate");
                sendErrorResponse(headerAccessor, "Invalid JWT token");
                return;
            }
            
            logger.info("👤 From User: {} ({})", fromUser.getId(), fromUser.getFname() + " " + fromUser.getLname());
            logger.info("🏠 Room ID: {}", roomId);
            logger.info("🧊 Candidate: {}", candidate.get("candidate"));
            
            // Handle missing toUserId - broadcast to all participants in room
            Object toUserIdObj = payload.get("toUserId");
            if (toUserIdObj != null) {
                // Send to specific user
                try {
                    UUID toUserId = UUID.fromString(toUserIdObj.toString());
                    User toUser = userService.getUserById(toUserId);
                    if (toUser == null) {
                        logger.error("❌ Target user not found: {}", toUserId);
                        sendErrorResponse(headerAccessor, "Target user not found");
                        return;
                    }
                    logger.info("👤 Target User: {} ({})", toUser.getId(), toUser.getFname() + " " + toUser.getLname());
                    signalingService.sendIceCandidate(roomId, fromUser, toUser, candidate);
                } catch (IllegalArgumentException e) {
                    logger.error("❌ Invalid UUID format: {}", e.getMessage());
                    sendErrorResponse(headerAccessor, "Invalid user ID format");
                    return;
                }
            } else {
                // Broadcast to all participants in room (for peer-to-peer calls)
                logger.info("📡 Broadcasting ICE candidate to all participants in room");
                signalingService.broadcastIceCandidate(roomId, fromUser, candidate);
            }
            logger.info("✅ ICE candidate handled successfully!");
            
        } catch (Exception e) {
            logger.error("❌ Failed to handle ICE candidate: {}", e.getMessage(), e);
            sendErrorResponse(headerAccessor, "Failed to handle ICE candidate: " + e.getMessage());
        }
    }
    
    // Handle join room
    @MessageMapping("/calls/join")
    public void handleJoinRoom(@Payload Map<String, Object> payload, SimpMessageHeaderAccessor headerAccessor) {
        try {
            logger.info("=== 📨 INCOMING JOIN ROOM MESSAGE ===");
            logger.info("📥 Received join room message: {}", payload);
            
            // Validate required fields
            String jwt = (String) payload.get("token");
            if (jwt == null) {
                logger.error("❌ No JWT token provided for join room");
                sendErrorResponse(headerAccessor, "No JWT token provided");
                return;
            }
            
            String roomId = (String) payload.get("roomId");
            if (roomId == null || roomId.trim().isEmpty()) {
                logger.error("❌ No roomId provided for join room");
                sendErrorResponse(headerAccessor, "No roomId provided");
                return;
            }
            
            // Authenticate user
            User user = userService.getUserFromToken(ensureBearerPrefix(jwt));
            if (user == null) {
                logger.error("❌ Invalid JWT token for join room");
                sendErrorResponse(headerAccessor, "Invalid JWT token");
                return;
            }
            
            logger.info("👤 User: {} ({}) joining room: {}", user.getId(), user.getFname() + " " + user.getLname(), roomId);
            
            // Notify other participants about user joining
            Map<String, Object> joinMessage = new HashMap<>();
            joinMessage.put("type", "user-joined");
            joinMessage.put("userId", user.getId());
            joinMessage.put("userName", user.getFname() + " " + user.getLname());
            joinMessage.put("roomId", roomId);
            joinMessage.put("timestamp", System.currentTimeMillis());
            
            // Broadcast to all participants in room (except the joining user)
            String destination = "/room/" + roomId + "/call-signaling";
            messagingTemplate.convertAndSend(destination, joinMessage);
            
            logger.info("✅ User join room handled successfully!");
            
        } catch (Exception e) {
            logger.error("❌ Failed to handle join room: {}", e.getMessage(), e);
            sendErrorResponse(headerAccessor, "Failed to handle join room: " + e.getMessage());
        }
    }
    
    // Handle leave room
    @MessageMapping("/calls/leave")
    public void handleLeaveRoom(@Payload Map<String, Object> payload, SimpMessageHeaderAccessor headerAccessor) {
        try {
            logger.info("=== 📨 INCOMING LEAVE ROOM MESSAGE ===");
            logger.info("📥 Received leave room message: {}", payload);
            
            // Validate required fields
            String jwt = (String) payload.get("token");
            if (jwt == null) {
                logger.error("❌ No JWT token provided for leave room");
                sendErrorResponse(headerAccessor, "No JWT token provided");
                return;
            }
            
            String roomId = (String) payload.get("roomId");
            if (roomId == null || roomId.trim().isEmpty()) {
                logger.error("❌ No roomId provided for leave room");
                sendErrorResponse(headerAccessor, "No roomId provided");
                return;
            }
            
            // Authenticate user
            User user = userService.getUserFromToken(ensureBearerPrefix(jwt));
            if (user == null) {
                logger.error("❌ Invalid JWT token for leave room");
                sendErrorResponse(headerAccessor, "Invalid JWT token");
                return;
            }
            
            logger.info("👤 User: {} ({}) leaving room: {}", user.getId(), user.getFname() + " " + user.getLname(), roomId);
            
            // Notify other participants about user leaving
            Map<String, Object> leaveMessage = new HashMap<>();
            leaveMessage.put("type", "user-left");
            leaveMessage.put("userId", user.getId());
            leaveMessage.put("userName", user.getFname() + " " + user.getLname());
            leaveMessage.put("roomId", roomId);
            leaveMessage.put("timestamp", System.currentTimeMillis());
            
            // Broadcast to all participants in room (except the leaving user)
            String destination = "/room/" + roomId + "/call-signaling";
            messagingTemplate.convertAndSend(destination, leaveMessage);
            
            logger.info("✅ User leave room handled successfully!");
            
        } catch (Exception e) {
            logger.error("❌ Failed to handle leave room: {}", e.getMessage(), e);
            sendErrorResponse(headerAccessor, "Failed to handle leave room: " + e.getMessage());
        }
    }
    
    // Handle call invitation
    @MessageMapping("/calls/invite")
    public void handleCallInvitation(@Payload Map<String, Object> payload, SimpMessageHeaderAccessor headerAccessor) {
        try {
            logger.info("=== 📨 INCOMING CALL INVITATION MESSAGE ===");
            logger.info("📥 Received call invitation message: {}", payload);
            
            // Validate required fields
            String jwt = (String) payload.get("token");
            if (jwt == null) {
                logger.error("❌ No JWT token provided for call invitation");
                sendErrorResponse(headerAccessor, "No JWT token provided");
                return;
            }
            
            String roomId = (String) payload.get("roomId");
            if (roomId == null || roomId.trim().isEmpty()) {
                logger.error("❌ No roomId provided for call invitation");
                sendErrorResponse(headerAccessor, "No roomId provided");
                return;
            }
            
            Object toUserIdObj = payload.get("toUserId");
            if (toUserIdObj == null) {
                logger.error("❌ No toUserId provided for call invitation");
                sendErrorResponse(headerAccessor, "No toUserId provided");
                return;
            }
            
            String callType = (String) payload.get("callType");
            if (callType == null || callType.trim().isEmpty()) {
                logger.error("❌ No callType provided for call invitation");
                sendErrorResponse(headerAccessor, "No callType provided");
                return;
            }
            
            // Authenticate user
            User fromUser = userService.getUserFromToken(ensureBearerPrefix(jwt));
            if (fromUser == null) {
                logger.error("❌ Invalid JWT token for call invitation");
                sendErrorResponse(headerAccessor, "Invalid JWT token");
                return;
            }
            
            UUID toUserId = UUID.fromString(toUserIdObj.toString());
            User toUser = userService.getUserById(toUserId);
            if (toUser == null) {
                logger.error("❌ Target user not found: {}", toUserId);
                sendErrorResponse(headerAccessor, "Target user not found");
                return;
            }
            
            // Parse call type
            com.bharat.springbootsocial.entity.CallRoom.CallType type;
            try {
                type = com.bharat.springbootsocial.entity.CallRoom.CallType.valueOf(callType);
            } catch (IllegalArgumentException e) {
                logger.error("❌ Invalid call type: {}", callType);
                sendErrorResponse(headerAccessor, "Invalid call type: " + callType);
                return;
            }
            
            logger.info("👤 From User: {} ({})", fromUser.getId(), fromUser.getFname() + " " + fromUser.getLname());
            logger.info("👤 To User: {} ({})", toUser.getId(), toUser.getFname() + " " + toUser.getLname());
            logger.info("🏠 Room ID: {}", roomId);
            logger.info("📞 Call Type: {}", callType);
            
            signalingService.sendCallInvitation(roomId, fromUser, toUser, type);
            logger.info("✅ Call invitation handled successfully!");
            
        } catch (IllegalArgumentException e) {
            logger.error("❌ Invalid UUID format: {}", e.getMessage());
            sendErrorResponse(headerAccessor, "Invalid user ID format");
        } catch (Exception e) {
            logger.error("❌ Failed to handle call invitation: {}", e.getMessage(), e);
            sendErrorResponse(headerAccessor, "Failed to handle call invitation: " + e.getMessage());
        }
    }
    
    // Handle call response (accept/decline)
    @MessageMapping("/calls/response")
    public void handleCallResponse(@Payload Map<String, Object> payload, SimpMessageHeaderAccessor headerAccessor) {
        try {
            logger.info("=== 📨 INCOMING CALL RESPONSE MESSAGE ===");
            logger.info("📥 Received call response message: {}", payload);
            logger.info("📥 Raw payload keys: {}", payload.keySet());
            logger.info("📥 Payload roomId: {}", payload.get("roomId"));
            logger.info("📥 Payload toUserId: {}", payload.get("toUserId"));
            logger.info("📥 Payload accepted: {}", payload.get("accepted"));
            
            // Validate required fields
            String jwt = (String) payload.get("token");
            if (jwt == null) {
                logger.error("❌ No JWT token provided for call response");
                sendErrorResponse(headerAccessor, "No JWT token provided");
                return;
            }
            
            String roomId = (String) payload.get("roomId");
            if (roomId == null || roomId.trim().isEmpty()) {
                logger.error("❌ No roomId provided for call response");
                sendErrorResponse(headerAccessor, "No roomId provided");
                return;
            }
            
            Object toUserIdObj = payload.get("toUserId");
            if (toUserIdObj == null) {
                logger.error("❌ No toUserId provided for call response");
                sendErrorResponse(headerAccessor, "No toUserId provided");
                return;
            }
            
            Object acceptedObj = payload.get("accepted");
            if (acceptedObj == null) {
                logger.error("❌ No accepted field provided for call response");
                sendErrorResponse(headerAccessor, "No accepted field provided");
                return;
            }
            
            // Authenticate user
            User fromUser = userService.getUserFromToken(ensureBearerPrefix(jwt));
            if (fromUser == null) {
                logger.error("❌ Invalid JWT token for call response");
                sendErrorResponse(headerAccessor, "Invalid JWT token");
                return;
            }
            
            UUID toUserId = UUID.fromString(toUserIdObj.toString());
            User toUser = userService.getUserById(toUserId);
            if (toUser == null) {
                logger.error("❌ Target user not found: {}", toUserId);
                sendErrorResponse(headerAccessor, "Target user not found");
                return;
            }
            
            Boolean accepted = Boolean.valueOf(acceptedObj.toString());
            
            logger.info("👤 From User: {} ({})", fromUser.getId(), fromUser.getFname() + " " + fromUser.getLname());
            logger.info("👤 To User: {} ({})", toUser.getId(), toUser.getFname() + " " + toUser.getLname());
            logger.info("🏠 Room ID: {}", roomId);
            logger.info("📞 Response: {}", accepted ? "accepted" : "declined");
            
            // Send call response through signaling service
            logger.info("🔔 Sending call response via signaling - From: {}, To: {}, Accepted: {}, RoomId: {}", 
                fromUser.getId(), toUser.getId(), accepted, roomId);
            signalingService.sendCallResponse(roomId, fromUser, toUser, accepted);
            logger.info("✅ Call response handled successfully via signaling service!");
            
        } catch (IllegalArgumentException e) {
            logger.error("❌ Invalid UUID format: {}", e.getMessage());
            sendErrorResponse(headerAccessor, "Invalid user ID format");
        } catch (Exception e) {
            logger.error("❌ Failed to handle call response: {}", e.getMessage(), e);
            sendErrorResponse(headerAccessor, "Failed to handle call response: " + e.getMessage());
        }
    }
    
    // Handle connection state update
    @MessageMapping("/calls/connection-state")
    public void handleConnectionStateUpdate(@Payload Map<String, Object> payload, SimpMessageHeaderAccessor headerAccessor) {
        try {
            logger.info("=== 📨 INCOMING CONNECTION STATE UPDATE MESSAGE ===");
            logger.info("📥 Received connection state update message: {}", payload);
            
            // Validate required fields
            String jwt = (String) payload.get("token");
            if (jwt == null) {
                logger.error("❌ No JWT token provided for connection state update");
                sendErrorResponse(headerAccessor, "No JWT token provided");
                return;
            }
            
            String roomId = (String) payload.get("roomId");
            if (roomId == null || roomId.trim().isEmpty()) {
                logger.error("❌ No roomId provided for connection state update");
                sendErrorResponse(headerAccessor, "No roomId provided");
                return;
            }
            
            String connectionState = (String) payload.get("connectionState");
            String iceConnectionState = (String) payload.get("iceConnectionState");
            
            // Authenticate user
            User user = userService.getUserFromToken(ensureBearerPrefix(jwt));
            if (user == null) {
                logger.error("❌ Invalid JWT token for connection state update");
                sendErrorResponse(headerAccessor, "Invalid JWT token");
                return;
            }
            
            logger.info("👤 User: {} ({})", user.getId(), user.getFname() + " " + user.getLname());
            logger.info("🏠 Room ID: {}", roomId);
            logger.info("🔗 Connection State: {}", connectionState);
            logger.info("🧊 ICE Connection State: {}", iceConnectionState);
            
            signalingService.updateCallSessionState(roomId, user, connectionState, iceConnectionState);
            logger.info("✅ Connection state updated successfully!");
            
        } catch (Exception e) {
            logger.error("❌ Failed to handle connection state update: {}", e.getMessage(), e);
            sendErrorResponse(headerAccessor, "Failed to handle connection state update: " + e.getMessage());
        }
    }
    
    // Handle broadcast message to room
    @MessageMapping("/calls/broadcast")
    public void handleBroadcast(@Payload Map<String, Object> payload, SimpMessageHeaderAccessor headerAccessor) {
        try {
            logger.info("=== 📨 INCOMING BROADCAST MESSAGE ===");
            logger.info("📥 Received broadcast message: {}", payload);
            
            // Validate required fields
            String jwt = (String) payload.get("token");
            if (jwt == null) {
                logger.error("❌ No JWT token provided for broadcast");
                sendErrorResponse(headerAccessor, "No JWT token provided");
                return;
            }
            
            String roomId = (String) payload.get("roomId");
            if (roomId == null || roomId.trim().isEmpty()) {
                logger.error("❌ No roomId provided for broadcast");
                sendErrorResponse(headerAccessor, "No roomId provided");
                return;
            }
            
            String messageType = (String) payload.get("messageType");
            if (messageType == null || messageType.trim().isEmpty()) {
                logger.error("❌ No messageType provided for broadcast");
                sendErrorResponse(headerAccessor, "No messageType provided");
                return;
            }
            
            Object dataObj = payload.get("data");
            Map<String, Object> data = null;
            if (dataObj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> dataMap = (Map<String, Object>) dataObj;
                data = dataMap;
            }
            
            // Authenticate user
            User fromUser = userService.getUserFromToken(ensureBearerPrefix(jwt));
            if (fromUser == null) {
                logger.error("❌ Invalid JWT token for broadcast");
                sendErrorResponse(headerAccessor, "Invalid JWT token");
                return;
            }
            
            logger.info("👤 From User: {} ({})", fromUser.getId(), fromUser.getFname() + " " + fromUser.getLname());
            logger.info("🏠 Room ID: {}", roomId);
            logger.info("📢 Message Type: {}", messageType);
            
            signalingService.broadcastToRoom(roomId, fromUser, messageType, data);
            logger.info("✅ Broadcast message handled successfully!");
            
        } catch (Exception e) {
            logger.error("❌ Failed to handle broadcast message: {}", e.getMessage(), e);
            sendErrorResponse(headerAccessor, "Failed to handle broadcast message: " + e.getMessage());
        }
    }
    
    // Handle call session subscription
    @MessageMapping("/calls/subscribe")
    public void handleCallSubscription(@Payload Map<String, Object> payload, SimpMessageHeaderAccessor headerAccessor) {
        try {
            logger.info("=== 📢 CALL SUBSCRIPTION REQUEST ===");
            logger.info("📥 Received call subscription: {}", payload);
            
            // Validate required fields
            String jwt = (String) payload.get("token");
            if (jwt == null) {
                logger.error("❌ No JWT token provided for call subscription");
                sendErrorResponse(headerAccessor, "No JWT token provided");
                return;
            }
            
            // Authenticate user
            User user = userService.getUserFromToken(ensureBearerPrefix(jwt));
            if (user == null) {
                logger.error("❌ Invalid JWT token for call subscription");
                sendErrorResponse(headerAccessor, "Invalid JWT token");
                return;
            }
            
            String roomId = (String) payload.get("roomId");
            
            logger.info("👤 User: {} ({})", user.getId(), user.getFname() + " " + user.getLname());
            logger.info("🏠 Room ID: {}", roomId);
            
            // Handle null roomId gracefully
            if (roomId == null) {
                logger.warn("⚠️ Room ID is null, using default value");
                roomId = "default-room";
            }
            
            // Send subscription confirmation
            Map<String, Object> confirmation = new HashMap<>();
            confirmation.put("type", "subscription-confirmed");
            confirmation.put("roomId", roomId);
            confirmation.put("userId", user.getId());
            confirmation.put("userName", user.getFname() + " " + user.getLname());
            confirmation.put("timestamp", System.currentTimeMillis());
            confirmation.put("message", "Successfully subscribed to call events");
            
            String destination = "/user/" + user.getId() + "/queue/call-signaling";
            messagingTemplate.convertAndSend(destination, confirmation);
            
            logger.info("✅ Call subscription confirmed and sent to: {}", destination);
            
        } catch (Exception e) {
            logger.error("❌ Failed to handle call subscription: {}", e.getMessage(), e);
            sendErrorResponse(headerAccessor, "Failed to handle call subscription: " + e.getMessage());
        }
    }
    
    // Handle call end
    @MessageMapping("/calls/end")
    public void handleCallEnd(@Payload Map<String, Object> payload, SimpMessageHeaderAccessor headerAccessor) {
        try {
            logger.info("=== 🔚 CALL END MESSAGE ===");
            logger.info("📥 Received call end message: {}", payload);
            
            // Validate required fields
            String jwt = (String) payload.get("token");
            if (jwt == null) {
                logger.error("❌ No JWT token provided for call end");
                sendErrorResponse(headerAccessor, "No JWT token provided");
                return;
            }
            
            String roomId = (String) payload.get("roomId");
            if (roomId == null || roomId.trim().isEmpty()) {
                logger.error("❌ No roomId provided for call end");
                sendErrorResponse(headerAccessor, "No roomId provided");
                return;
            }
            
            Object toUserIdObj = payload.get("toUserId");
            if (toUserIdObj == null) {
                logger.error("❌ No toUserId provided for call end");
                sendErrorResponse(headerAccessor, "No toUserId provided");
                return;
            }
            
            // Authenticate user
            User fromUser = userService.getUserFromToken(ensureBearerPrefix(jwt));
            if (fromUser == null) {
                logger.error("❌ Invalid JWT token for call end");
                sendErrorResponse(headerAccessor, "Invalid JWT token");
                return;
            }
            
            UUID toUserId = UUID.fromString(toUserIdObj.toString());
            String reason = (String) payload.getOrDefault("reason", "user-ended");
            Long timestamp = (Long) payload.getOrDefault("timestamp", System.currentTimeMillis());
            
            logger.info("👤 From User: {} ({})", fromUser.getId(), fromUser.getFname() + " " + fromUser.getLname());
            logger.info("👤 To User ID: {}", toUserId);
            logger.info("🏠 Room ID: {}", roomId);
            logger.info("🔚 End Reason: {}", reason);
            logger.info("⏰ Timestamp: {}", timestamp);
            
            // Create call end message
            Map<String, Object> endMessage = new HashMap<>();
            endMessage.put("type", "call-end");
            endMessage.put("timestamp", timestamp);
            endMessage.put("data", Map.of(
                "roomId", roomId,
                "from", fromUser.getId().toString(),
                "fromName", fromUser.getFname() + " " + fromUser.getLname(),
                "reason", reason
            ));
            
            // Send call end message to the other participant
            String destination = "/user/" + toUserId + "/queue/call-invitations";
            messagingTemplate.convertAndSend(destination, endMessage);
            
            logger.info("✅ Call end message sent to: {}", destination);
            
            // Also send to room events for logging/analytics
            Map<String, Object> roomEvent = new HashMap<>();
            roomEvent.put("type", "call-ended");
            roomEvent.put("roomId", roomId);
            roomEvent.put("timestamp", timestamp);
            roomEvent.put("data", Map.of(
                "endedBy", fromUser.getId().toString(),
                "endedByName", fromUser.getFname() + " " + fromUser.getLname(),
                "reason", reason
            ));
            
            String roomDestination = "/user/" + toUserId + "/queue/room-events";
            messagingTemplate.convertAndSend(roomDestination, roomEvent);
            
            logger.info("✅ Call end room event sent to: {}", roomDestination);
            
        } catch (Exception e) {
            logger.error("❌ Failed to handle call end: {}", e.getMessage(), e);
            sendErrorResponse(headerAccessor, "Failed to handle call end: " + e.getMessage());
        }
    }
}