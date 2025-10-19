package com.bharat.springbootsocial.controller;

import com.bharat.springbootsocial.entity.User;
import com.bharat.springbootsocial.services.GroupCallService;
import com.bharat.springbootsocial.services.ServiceInt;
import com.bharat.springbootsocial.services.WebRTCSignalingService;
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
public class GroupCallWebSocketController {
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    @Autowired
    private ServiceInt userService;
    
    @Autowired
    private WebRTCSignalingService signalingService;
    
    @Autowired
    private GroupCallService groupCallService;
    
    // Helper method to ensure JWT token has Bearer prefix
    private String ensureBearerPrefix(String jwt) {
        if (jwt != null && !jwt.startsWith("Bearer ")) {
            return "Bearer " + jwt;
        }
        return jwt;
    }
    
    // Handle group call invitation (NEW ENDPOINT)
    @MessageMapping("/group-calls/invite")
    public void handleGroupCallInvite(@Payload Map<String, Object> payload, SimpMessageHeaderAccessor headerAccessor) {
        try {
            System.out.println("=== 📞 GROUP CALL INVITE MESSAGE ===");
            System.out.println("📥 Received group call invite: " + payload);
            
            String jwt = (String) payload.get("token");
            if (jwt == null) {
                System.err.println("❌ No JWT token provided for group call invite");
                return;
            }
            
            User fromUser = userService.getUserFromToken(ensureBearerPrefix(jwt));
            String roomId = (String) payload.get("roomId");
            UUID toUserId = UUID.fromString(payload.get("toUserId").toString());
            String callType = (String) payload.get("callType");
            UUID groupId = payload.get("groupId") != null ? UUID.fromString(payload.get("groupId").toString()) : null;
            
            System.out.println("👤 From User: " + fromUser.getId() + " (" + fromUser.getFname() + " " + fromUser.getLname() + ")");
            System.out.println("👤 To User ID: " + toUserId);
            System.out.println("🏠 Room ID: " + roomId);
            System.out.println("👥 Group ID: " + groupId);
            System.out.println("📞 Call Type: " + callType);
            
            User toUser = userService.getUserById(toUserId);
            if (toUser == null) {
                System.err.println("❌ Target user not found: " + toUserId);
                return;
            }
            
            // Send invitation to call-invitations queue (same as one-to-one calls)
            String destination = "/user/" + toUserId + "/queue/call-invitations";
            
            // Create invitation data (matching frontend expectations)
            Map<String, Object> invitationData = new HashMap<>();
            invitationData.put("roomId", roomId);
            invitationData.put("callType", callType);
            invitationData.put("groupId", groupId);
            invitationData.put("messageType", "GROUP_CALL_INVITATION");
            
            // Create invitation message (matching frontend expectations)
            Map<String, Object> invitationMessage = new HashMap<>();
            invitationMessage.put("id", System.currentTimeMillis()); // Unique ID
            invitationMessage.put("type", "GROUP_CALL_INVITATION"); // CRITICAL: Must be this type
            invitationMessage.put("relatedEntityType", "GROUP_CALL"); // CRITICAL: Must be this type
            invitationMessage.put("title", "Incoming Group Call");
            invitationMessage.put("message", fromUser.getFname() + " " + fromUser.getLname() + " is calling the group");
            invitationMessage.put("relatedEntityId", roomId);
            invitationMessage.put("data", invitationData);
            invitationMessage.put("sender", Map.of(
                "id", fromUser.getId(),
                "fname", fromUser.getFname(),
                "lname", fromUser.getLname(),
                "profileImage", fromUser.getProfileImage() != null ? fromUser.getProfileImage() : ""
            ));
            invitationMessage.put("recipient", Map.of(
                "id", toUser.getId(),
                "fname", toUser.getFname(),
                "lname", toUser.getLname()
            ));
            invitationMessage.put("isRead", false);
            invitationMessage.put("createdAt", java.time.LocalDateTime.now().toString());
            
            messagingTemplate.convertAndSend(destination, invitationMessage);
            System.out.println("✅ Group call invitation sent to: " + destination);
            System.out.println("📨 Invitation message: " + invitationMessage);
            System.out.println("================================");
            
        } catch (Exception e) {
            System.err.println("❌ Failed to handle group call invite: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Handle group call room subscription
    @MessageMapping("/group-calls/subscribe")
    public void handleGroupCallSubscription(@Payload Map<String, Object> payload, SimpMessageHeaderAccessor headerAccessor) {
        try {
            System.out.println("=== 📢 GROUP CALL SUBSCRIPTION REQUEST ===");
            System.out.println("📥 Received group call subscription: " + payload);
            
            String jwt = (String) payload.get("token");
            if (jwt == null) {
                System.err.println("❌ No JWT token provided for group call subscription");
                return;
            }
            
            User user = userService.getUserFromToken(ensureBearerPrefix(jwt));
            String roomId = (String) payload.get("roomId");
            UUID groupId = payload.get("groupId") != null ? UUID.fromString(payload.get("groupId").toString()) : null;
            
            System.out.println("👤 User: " + user.getId() + " (" + user.getFname() + " " + user.getLname() + ")");
            System.out.println("🏠 Room ID: " + roomId);
            System.out.println("👥 Group ID: " + groupId);
            
            // Handle null roomId gracefully
            if (roomId == null) {
                System.out.println("⚠️ Room ID is null, using default value");
                roomId = "default-group-room";
            }
            
            // Send subscription confirmation
            Map<String, Object> confirmation = new HashMap<>();
            confirmation.put("type", "group-call-subscription-confirmed");
            confirmation.put("roomId", roomId);
            confirmation.put("groupId", groupId);
            confirmation.put("userId", user.getId());
            confirmation.put("userName", user.getFname() + " " + user.getLname());
            confirmation.put("timestamp", System.currentTimeMillis());
            confirmation.put("message", "Successfully subscribed to group call events");
            
            String destination = "/user/" + user.getId() + "/queue/call-signaling";
            messagingTemplate.convertAndSend(destination, confirmation);
            
            System.out.println("✅ Group call subscription confirmed and sent to: " + destination);
            System.out.println("================================");
            
        } catch (Exception e) {
            System.err.println("❌ Failed to handle group call subscription: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Handle group call WebRTC offer
    @MessageMapping("/group-calls/offer")
    public void handleGroupCallOffer(@Payload Map<String, Object> payload, SimpMessageHeaderAccessor headerAccessor) {
        try {
            System.out.println("=== 📨 GROUP CALL OFFER MESSAGE ===");
            System.out.println("📥 Received group call offer: " + payload);
            
            String jwt = (String) payload.get("token");
            if (jwt == null) {
                System.err.println("❌ No JWT token provided for group call offer");
                return;
            }
            
            User fromUser = userService.getUserFromToken(ensureBearerPrefix(jwt));
            String roomId = (String) payload.get("roomId");
            UUID toUserId = UUID.fromString(payload.get("toUserId").toString());
            Map<String, Object> offer = (Map<String, Object>) payload.get("offer");
            
            System.out.println("👤 From User: " + fromUser.getId() + " (" + fromUser.getFname() + " " + fromUser.getLname() + ")");
            System.out.println("👤 To User ID: " + toUserId);
            System.out.println("🏠 Room ID: " + roomId);
            
            User toUser = userService.getUserById(toUserId);
            if (toUser == null) {
                System.err.println("❌ Target user not found: " + toUserId);
                return;
            }
            
            // Use WebRTCSignalingService for proper offer handling
            signalingService.sendOffer(roomId, fromUser, toUser, offer);
            
            System.out.println("✅ Group call offer sent to user " + toUserId);
            System.out.println("================================");
            
        } catch (Exception e) {
            System.err.println("❌ Failed to handle group call offer: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Handle group call WebRTC answer
    @MessageMapping("/group-calls/answer")
    public void handleGroupCallAnswer(@Payload Map<String, Object> payload, SimpMessageHeaderAccessor headerAccessor) {
        try {
            System.out.println("=== 📨 GROUP CALL ANSWER MESSAGE ===");
            System.out.println("📥 Received group call answer: " + payload);
            
            String jwt = (String) payload.get("token");
            if (jwt == null) {
                System.err.println("❌ No JWT token provided for group call answer");
                return;
            }
            
            User fromUser = userService.getUserFromToken(ensureBearerPrefix(jwt));
            String roomId = (String) payload.get("roomId");
            UUID toUserId = UUID.fromString(payload.get("toUserId").toString());
            Map<String, Object> answer = (Map<String, Object>) payload.get("answer");
            
            System.out.println("👤 From User: " + fromUser.getId() + " (" + fromUser.getFname() + " " + fromUser.getLname() + ")");
            System.out.println("👤 To User ID: " + toUserId);
            System.out.println("🏠 Room ID: " + roomId);
            
            User toUser = userService.getUserById(toUserId);
            if (toUser == null) {
                System.err.println("❌ Target user not found: " + toUserId);
                return;
            }
            
            // Use WebRTCSignalingService for proper answer handling
            signalingService.sendAnswer(roomId, fromUser, toUser, answer);
            
            System.out.println("✅ Group call answer sent to user " + toUserId);
            System.out.println("================================");
            
        } catch (Exception e) {
            System.err.println("❌ Failed to handle group call answer: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Handle group call ICE candidate
    @MessageMapping("/group-calls/ice-candidate")
    public void handleGroupCallIceCandidate(@Payload Map<String, Object> payload, SimpMessageHeaderAccessor headerAccessor) {
        try {
            System.out.println("=== 📨 GROUP CALL ICE CANDIDATE MESSAGE ===");
            System.out.println("📥 Received group call ICE candidate: " + payload);
            
            String jwt = (String) payload.get("token");
            if (jwt == null) {
                System.err.println("❌ No JWT token provided for group call ICE candidate");
                return;
            }
            
            User fromUser = userService.getUserFromToken(ensureBearerPrefix(jwt));
            String roomId = (String) payload.get("roomId");
            UUID toUserId = UUID.fromString(payload.get("toUserId").toString());
            Map<String, Object> candidate = (Map<String, Object>) payload.get("candidate");
            
            System.out.println("👤 From User: " + fromUser.getId() + " (" + fromUser.getFname() + " " + fromUser.getLname() + ")");
            System.out.println("👤 To User ID: " + toUserId);
            System.out.println("🏠 Room ID: " + roomId);
            
            User toUser = userService.getUserById(toUserId);
            if (toUser == null) {
                System.err.println("❌ Target user not found: " + toUserId);
                return;
            }
            
            // Use WebRTCSignalingService for proper ICE candidate handling
            signalingService.sendIceCandidate(roomId, fromUser, toUser, candidate);
            
            System.out.println("✅ Group call ICE candidate sent to user " + toUserId);
            System.out.println("================================");
            
        } catch (Exception e) {
            System.err.println("❌ Failed to handle group call ICE candidate: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Handle group call room events
    @MessageMapping("/group-calls/room-events")
    public void handleGroupCallRoomEvents(@Payload Map<String, Object> payload, SimpMessageHeaderAccessor headerAccessor) {
        try {
            System.out.println("=== 📢 GROUP CALL ROOM EVENTS ===");
            System.out.println("📥 Received group call room event: " + payload);
            
            String jwt = (String) payload.get("token");
            if (jwt == null) {
                System.err.println("❌ No JWT token provided for group call room events");
                return;
            }
            
            User user = userService.getUserFromToken(ensureBearerPrefix(jwt));
            String roomId = (String) payload.get("roomId");
            String eventType = (String) payload.get("eventType");
            
            System.out.println("👤 User: " + user.getId() + " (" + user.getFname() + " " + user.getLname() + ")");
            System.out.println("🏠 Room ID: " + roomId);
            System.out.println("📢 Event Type: " + eventType);
            
            // Broadcast room event to all participants
            Map<String, Object> roomEvent = new HashMap<>();
            roomEvent.put("type", "group-call-room-event");
            roomEvent.put("roomId", roomId);
            roomEvent.put("eventType", eventType);
            roomEvent.put("userId", user.getId());
            roomEvent.put("userName", user.getFname() + " " + user.getLname());
            roomEvent.put("timestamp", System.currentTimeMillis());
            roomEvent.put("data", payload.get("data"));
            
            // Send to room participants (same pattern as one-to-one calls)
            String roomDestination = "/room/" + roomId + "/call-signaling";
            messagingTemplate.convertAndSend(roomDestination, roomEvent);
            
            System.out.println("✅ Group call room event broadcasted to: " + roomDestination);
            System.out.println("================================");
            
        } catch (Exception e) {
            System.err.println("❌ Failed to handle group call room events: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Handle group call session state update
    @MessageMapping("/group-calls/session-state")
    public void handleGroupCallSessionState(@Payload Map<String, Object> payload, SimpMessageHeaderAccessor headerAccessor) {
        try {
            System.out.println("=== 🔗 GROUP CALL SESSION STATE UPDATE ===");
            System.out.println("📥 Received group call session state: " + payload);
            
            String jwt = (String) payload.get("token");
            if (jwt == null) {
                System.err.println("❌ No JWT token provided for group call session state");
                return;
            }
            
            User user = userService.getUserFromToken(ensureBearerPrefix(jwt));
            String roomId = (String) payload.get("roomId");
            String connectionState = (String) payload.get("connectionState");
            String iceConnectionState = (String) payload.get("iceConnectionState");
            
            System.out.println("👤 User: " + user.getId() + " (" + user.getFname() + " " + user.getLname() + ")");
            System.out.println("🏠 Room ID: " + roomId);
            System.out.println("🔗 Connection State: " + connectionState);
            System.out.println("🧊 ICE Connection State: " + iceConnectionState);
            
            // Update session state
            groupCallService.updateGroupCallSessionState(roomId, user, connectionState, iceConnectionState);
            
            // Broadcast state update to room participants
            Map<String, Object> stateUpdate = new HashMap<>();
            stateUpdate.put("type", "group-call-session-state");
            stateUpdate.put("roomId", roomId);
            stateUpdate.put("userId", user.getId());
            stateUpdate.put("userName", user.getFname() + " " + user.getLname());
            stateUpdate.put("connectionState", connectionState);
            stateUpdate.put("iceConnectionState", iceConnectionState);
            stateUpdate.put("timestamp", System.currentTimeMillis());
            
            String roomDestination = "/room/" + roomId + "/call-signaling";
            messagingTemplate.convertAndSend(roomDestination, stateUpdate);
            
            System.out.println("✅ Group call session state updated and broadcasted to: " + roomDestination);
            System.out.println("================================");
            
        } catch (Exception e) {
            System.err.println("❌ Failed to handle group call session state: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
