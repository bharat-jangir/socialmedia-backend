package com.bharat.springbootsocial.services;

import com.bharat.springbootsocial.entity.CallRoom;
import com.bharat.springbootsocial.entity.CallSession;
import com.bharat.springbootsocial.entity.GroupCallRoom;
import com.bharat.springbootsocial.entity.User;
import com.bharat.springbootsocial.exception.UserException;
import com.bharat.springbootsocial.repository.CallSessionRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class WebRTCSignalingService {
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    @Autowired
    private CallRoomService callRoomService;
    
    @Autowired
    private GroupCallService groupCallService;
    
    @Autowired
    private CallSessionRepo callSessionRepo;
    
    // Helper method to determine if room ID is for a group call
    private boolean isGroupCallRoom(String roomId) {
        return roomId != null && roomId.startsWith("group_call_");
    }
    
    // Helper method to determine if room ID is for a regular call
    private boolean isRegularCallRoom(String roomId) {
        return roomId != null && roomId.startsWith("room_");
    }
    
    // Helper method to validate room exists (works for both regular and group calls)
    private void validateRoomExists(String roomId) throws UserException {
        if (isGroupCallRoom(roomId)) {
            groupCallService.getGroupCallRoomById(roomId);
        } else {
            callRoomService.getCallRoomById(roomId);
        }
    }
    
    // Send WebRTC offer to specific user
    public void sendOffer(String roomId, User fromUser, User toUser, Map<String, Object> offer) throws UserException {
        try {
            // Validate room exists (works for both regular and group calls)
            validateRoomExists(roomId);
            // Note: Skipping participant validation to avoid LazyInitializationException
            // Participants are already validated when they join the room
            
            // Log offer details
            System.out.println("=== 🎯 WebRTC OFFER DEBUG ===");
            System.out.println("📤 Sending offer from User " + fromUser.getId() + " (" + fromUser.getFname() + " " + fromUser.getLname() + ")");
            System.out.println("📥 To User " + toUser.getId() + " (" + toUser.getFname() + " " + toUser.getLname() + ")");
            System.out.println("🏠 Room ID: " + roomId);
            System.out.println("📋 Offer Type: " + offer.get("type"));
            System.out.println("📋 Offer SDP Length: " + (offer.get("sdp") != null ? offer.get("sdp").toString().length() : "null"));
            System.out.println("⏰ Timestamp: " + System.currentTimeMillis());
            
            // Create signaling message
            Map<String, Object> signalingMessage = Map.of(
                "type", "offer",
                "from", fromUser.getId(),
                "fromName", fromUser.getFname() + " " + fromUser.getLname(),
                "roomId", roomId,
                "offer", offer,
                "timestamp", System.currentTimeMillis()
            );
            
            // Send to specific user
            String destination = "/user/" + toUser.getId() + "/queue/call-signaling";
            System.out.println("📡 WebSocket Destination: " + destination);
            messagingTemplate.convertAndSend(destination, signalingMessage);
            
            System.out.println("✅ WebRTC offer sent successfully!");
            System.out.println("================================");
            
        } catch (Exception e) {
            System.err.println("❌ Failed to send WebRTC offer: " + e.getMessage());
            e.printStackTrace();
            throw new UserException("Failed to send WebRTC offer: " + e.getMessage());
        }
    }
    
    // Send WebRTC answer to specific user
    public void sendAnswer(String roomId, User fromUser, User toUser, Map<String, Object> answer) throws UserException {
        try {
            // Validate room exists (works for both regular and group calls)
            validateRoomExists(roomId);
            // Note: Skipping participant validation to avoid LazyInitializationException
            // Participants are already validated when they join the room
            
            // Log answer details
            System.out.println("=== 🎯 WebRTC ANSWER DEBUG ===");
            System.out.println("📤 Sending answer from User " + fromUser.getId() + " (" + fromUser.getFname() + " " + fromUser.getLname() + ")");
            System.out.println("📥 To User " + toUser.getId() + " (" + toUser.getFname() + " " + toUser.getLname() + ")");
            System.out.println("🏠 Room ID: " + roomId);
            System.out.println("📋 Answer Type: " + answer.get("type"));
            System.out.println("📋 Answer SDP Length: " + (answer.get("sdp") != null ? answer.get("sdp").toString().length() : "null"));
            System.out.println("⏰ Timestamp: " + System.currentTimeMillis());
            
            // Create signaling message
            Map<String, Object> signalingMessage = Map.of(
                "type", "answer",
                "from", fromUser.getId(),
                "fromName", fromUser.getFname() + " " + fromUser.getLname(),
                "roomId", roomId,
                "answer", answer,
                "timestamp", System.currentTimeMillis()
            );
            
            // Send to specific user
            String destination = "/user/" + toUser.getId() + "/queue/call-signaling";
            System.out.println("📡 WebSocket Destination: " + destination);
            messagingTemplate.convertAndSend(destination, signalingMessage);
            
            System.out.println("✅ WebRTC answer sent successfully!");
            System.out.println("================================");
            
        } catch (Exception e) {
            System.err.println("❌ Failed to send WebRTC answer: " + e.getMessage());
            e.printStackTrace();
            throw new UserException("Failed to send WebRTC answer: " + e.getMessage());
        }
    }
    
    // Send ICE candidate to specific user
    public void sendIceCandidate(String roomId, User fromUser, User toUser, Map<String, Object> iceCandidate) throws UserException {
        try {
            // Validate room exists (works for both regular and group calls)
            validateRoomExists(roomId);
            // Note: Skipping participant validation to avoid LazyInitializationException
            // Participants are already validated when they join the room
            
            // Log ICE candidate details
            System.out.println("=== 🧊 ICE CANDIDATE DEBUG ===");
            System.out.println("📤 Sending ICE candidate from User " + fromUser.getId() + " (" + fromUser.getFname() + " " + fromUser.getLname() + ")");
            System.out.println("📥 To User " + toUser.getId() + " (" + toUser.getFname() + " " + toUser.getLname() + ")");
            System.out.println("🏠 Room ID: " + roomId);
            System.out.println("🧊 Candidate: " + iceCandidate.get("candidate"));
            System.out.println("🧊 SDP MLine Index: " + iceCandidate.get("sdpMLineIndex"));
            System.out.println("🧊 SDP Mid: " + iceCandidate.get("sdpMid"));
            System.out.println("⏰ Timestamp: " + System.currentTimeMillis());
            
            // Create signaling message
            Map<String, Object> signalingMessage = Map.of(
                "type", "ice-candidate",
                "from", fromUser.getId(),
                "fromName", fromUser.getFname() + " " + fromUser.getLname(),
                "roomId", roomId,
                "candidate", iceCandidate,
                "timestamp", System.currentTimeMillis()
            );
            
            // Send to specific user
            String destination = "/user/" + toUser.getId() + "/queue/call-signaling";
            System.out.println("📡 WebSocket Destination: " + destination);
            messagingTemplate.convertAndSend(destination, signalingMessage);
            
            System.out.println("✅ ICE candidate sent successfully!");
            System.out.println("================================");
            
        } catch (Exception e) {
            System.err.println("❌ Failed to send ICE candidate: " + e.getMessage());
            e.printStackTrace();
            throw new UserException("Failed to send ICE candidate: " + e.getMessage());
        }
    }
    
    // Broadcast answer to all participants in room (except sender)
    public void broadcastAnswer(String roomId, User fromUser, Map<String, Object> answer) throws UserException {
        try {
            // Validate room exists (works for both regular and group calls)
            validateRoomExists(roomId);
            
            // Log broadcast details
            System.out.println("=== 📡 BROADCAST ANSWER DEBUG ===");
            System.out.println("📤 Broadcasting answer from User " + fromUser.getId() + " (" + fromUser.getFname() + " " + fromUser.getLname() + ")");
            System.out.println("🏠 Room ID: " + roomId);
            System.out.println("📋 Answer Type: " + answer.get("type"));
            System.out.println("⏰ Timestamp: " + System.currentTimeMillis());
            
            // Create signaling message
            Map<String, Object> signalingMessage = Map.of(
                "type", "answer",
                "from", fromUser.getId(),
                "fromName", fromUser.getFname() + " " + fromUser.getLname(),
                "roomId", roomId,
                "answer", answer,
                "timestamp", System.currentTimeMillis()
            );
            
            // Broadcast to all participants in room (except sender)
            String destination = "/room/" + roomId + "/call-signaling";
            System.out.println("📡 WebSocket Destination: " + destination);
            messagingTemplate.convertAndSend(destination, signalingMessage);
            
            System.out.println("✅ Answer broadcasted successfully!");
            System.out.println("================================");
            
        } catch (Exception e) {
            System.err.println("❌ Failed to broadcast answer: " + e.getMessage());
            e.printStackTrace();
            throw new UserException("Failed to broadcast answer: " + e.getMessage());
        }
    }
    
    // Broadcast ICE candidate to all participants in room (except sender)
    public void broadcastIceCandidate(String roomId, User fromUser, Map<String, Object> iceCandidate) throws UserException {
        try {
            // Validate room exists (works for both regular and group calls)
            validateRoomExists(roomId);
            
            // Log broadcast details
            System.out.println("=== 📡 BROADCAST ICE CANDIDATE DEBUG ===");
            System.out.println("📤 Broadcasting ICE candidate from User " + fromUser.getId() + " (" + fromUser.getFname() + " " + fromUser.getLname() + ")");
            System.out.println("🏠 Room ID: " + roomId);
            System.out.println("🧊 Candidate: " + iceCandidate.get("candidate"));
            System.out.println("⏰ Timestamp: " + System.currentTimeMillis());
            
            // Create signaling message
            Map<String, Object> signalingMessage = Map.of(
                "type", "ice-candidate",
                "from", fromUser.getId(),
                "fromName", fromUser.getFname() + " " + fromUser.getLname(),
                "roomId", roomId,
                "candidate", iceCandidate,
                "timestamp", System.currentTimeMillis()
            );
            
            // Broadcast to all participants in room (except sender)
            String destination = "/room/" + roomId + "/call-signaling";
            System.out.println("📡 WebSocket Destination: " + destination);
            messagingTemplate.convertAndSend(destination, signalingMessage);
            
            System.out.println("✅ ICE candidate broadcasted successfully!");
            System.out.println("================================");
            
        } catch (Exception e) {
            System.err.println("❌ Failed to broadcast ICE candidate: " + e.getMessage());
            e.printStackTrace();
            throw new UserException("Failed to broadcast ICE candidate: " + e.getMessage());
        }
    }
    
    // Broadcast message to all room participants
    public void broadcastToRoom(String roomId, User fromUser, String messageType, Map<String, Object> data) throws UserException {
        try {
            List<User> participants;
            
            if (isGroupCallRoom(roomId)) {
                GroupCallRoom groupRoom = groupCallService.getGroupCallRoomById(roomId);
                if (!groupRoom.isParticipant(fromUser)) {
                    throw new UserException("User not authorized for this room");
                }
                participants = groupRoom.getParticipants();
            } else {
                CallRoom room = callRoomService.getCallRoomById(roomId);
                if (!room.isParticipant(fromUser)) {
                    throw new UserException("User not authorized for this room");
                }
                participants = room.getParticipants();
            }
            
            // Create broadcast message
            Map<String, Object> broadcastMessage = Map.of(
                "type", messageType,
                "from", fromUser.getId(),
                "fromName", fromUser.getFname() + " " + fromUser.getLname(),
                "roomId", roomId,
                "data", data,
                "timestamp", System.currentTimeMillis()
            );
            
            // Send to all participants except sender
            for (User participant : participants) {
                if (!participant.getId().equals(fromUser.getId())) {
                    String destination = "/user/" + participant.getId() + "/queue/call-signaling";
                    messagingTemplate.convertAndSend(destination, broadcastMessage);
                }
            }
            
            System.out.println("Broadcast message sent to room " + roomId + " from user " + fromUser.getId());
            
        } catch (Exception e) {
            throw new UserException("Failed to broadcast message: " + e.getMessage());
        }
    }
    
    // Send call invitation
    public void sendCallInvitation(String roomId, User fromUser, User toUser, CallRoom.CallType callType) throws UserException {
        try {
            // For call invitations, we only support regular call rooms
            CallRoom room = callRoomService.getCallRoomById(roomId);
            
            // Debug logging
            System.out.println("=== DEBUG: Sending Call Invitation ===");
            System.out.println("Room ID parameter: " + roomId);
            System.out.println("Room ID from room object: " + room.getRoomId());
            System.out.println("Room UUID: " + room.getId());
            System.out.println("Room Name: " + room.getRoomName());
            System.out.println("===============================");
            
            Map<String, Object> invitationData = Map.of(
                "roomId", roomId,
                "roomName", room.getRoomName(),
                "callType", callType.toString(),
                "from", fromUser.getId(),
                "fromName", fromUser.getFname() + " " + fromUser.getLname(),
                "fromProfileImage", fromUser.getProfileImage() != null ? fromUser.getProfileImage() : ""
            );
            
            Map<String, Object> invitationMessage = Map.of(
                "type", "call-invitation",
                "data", invitationData,
                "timestamp", System.currentTimeMillis()
            );
            
            String destination = "/user/" + toUser.getId() + "/queue/call-invitations";
            System.out.println("Sending call invitation to destination: " + destination);
            System.out.println("Call invitation message: " + invitationMessage);
            messagingTemplate.convertAndSend(destination, invitationMessage);
            
            System.out.println("Call invitation sent to user " + toUser.getId() + " from user " + fromUser.getId() + " via WebSocket");
            
        } catch (Exception e) {
            throw new UserException("Failed to send call invitation: " + e.getMessage());
        }
    }
    
    // Send call response (accept/decline)
    public void sendCallResponse(String roomId, User fromUser, User toUser, boolean accepted) throws UserException {
        try {
            Map<String, Object> responseData = Map.of(
                "roomId", roomId,
                "accepted", accepted,
                "from", fromUser.getId(),
                "fromName", fromUser.getFname() + " " + fromUser.getLname()
            );
            
            Map<String, Object> responseMessage = Map.of(
                "type", "call-response",
                "data", responseData,
                "timestamp", System.currentTimeMillis()
            );
            
            String destination = "/user/" + toUser.getId() + "/queue/call-invitations";
            messagingTemplate.convertAndSend(destination, responseMessage);
            
            System.out.println("Call response sent to user " + toUser.getId() + " from user " + fromUser.getId() + ": " + (accepted ? "accepted" : "declined"));
            
        } catch (Exception e) {
            throw new UserException("Failed to send call response: " + e.getMessage());
        }
    }
    
    // Update call session state
    public void updateCallSessionState(String roomId, User user, String connectionState, String iceConnectionState) throws UserException {
        try {
            if (isGroupCallRoom(roomId)) {
                // For group calls, delegate to GroupCallService
                groupCallService.updateGroupCallSessionState(roomId, user, connectionState, iceConnectionState);
                return;
            }
            
            // For regular calls
            CallRoom room = callRoomService.getCallRoomById(roomId);
            Optional<CallSession> sessionOpt = callSessionRepo.findByRoomAndUser(room, user);
            
            if (sessionOpt.isPresent()) {
                CallSession session = sessionOpt.get();
                session.setConnectionState(connectionState);
                session.setIceConnectionState(iceConnectionState);
                session.updateActivity();
                callSessionRepo.save(session);
                
                System.out.println("Call session state updated for user " + user.getId() + " in room " + roomId);
            }
            
        } catch (Exception e) {
            throw new UserException("Failed to update call session state: " + e.getMessage());
        }
    }
}