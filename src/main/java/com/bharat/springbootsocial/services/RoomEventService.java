package com.bharat.springbootsocial.services;

import com.bharat.springbootsocial.entity.CallRoom;
import com.bharat.springbootsocial.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class RoomEventService {
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    // Send user joined room event
    public void sendUserJoinedEvent(CallRoom room, User user) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("type", "user-joined");
            event.put("roomId", room.getRoomId());
            event.put("userId", user.getId());
            event.put("userName", user.getFname() + " " + user.getLname());
            event.put("timestamp", System.currentTimeMillis());
            event.put("participantCount", room.getParticipants().size());
            event.put("maxParticipants", room.getMaxParticipants());
            
            // Send to all room participants except the one who joined
            room.getParticipants().forEach(participant -> {
                if (!participant.getId().equals(user.getId())) {
                    String destination = "/user/" + participant.getId() + "/queue/room-events";
                    messagingTemplate.convertAndSend(destination, event);
                    System.out.println("📢 Room event sent to user " + participant.getId() + ": user-joined");
                }
            });
            
            System.out.println("✅ User joined room event sent successfully");
            
        } catch (Exception e) {
            System.err.println("❌ Failed to send user joined event: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Send user left room event
    public void sendUserLeftEvent(CallRoom room, User user) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("type", "user-left");
            event.put("roomId", room.getRoomId());
            event.put("userId", user.getId());
            event.put("userName", user.getFname() + " " + user.getLname());
            event.put("timestamp", System.currentTimeMillis());
            event.put("participantCount", room.getParticipants().size());
            event.put("maxParticipants", room.getMaxParticipants());
            
            // Send to all remaining room participants
            room.getParticipants().forEach(participant -> {
                String destination = "/user/" + participant.getId() + "/queue/room-events";
                messagingTemplate.convertAndSend(destination, event);
                System.out.println("📢 Room event sent to user " + participant.getId() + ": user-left");
            });
            
            System.out.println("✅ User left room event sent successfully");
            
        } catch (Exception e) {
            System.err.println("❌ Failed to send user left event: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Send room status changed event
    public void sendRoomStatusChangedEvent(CallRoom room, String oldStatus, String newStatus) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("type", "room-status-changed");
            event.put("roomId", room.getRoomId());
            event.put("oldStatus", oldStatus);
            event.put("newStatus", newStatus);
            event.put("timestamp", System.currentTimeMillis());
            event.put("participantCount", room.getParticipants().size());
            event.put("maxParticipants", room.getMaxParticipants());
            
            // Send to all room participants
            room.getParticipants().forEach(participant -> {
                String destination = "/user/" + participant.getId() + "/queue/room-events";
                messagingTemplate.convertAndSend(destination, event);
                System.out.println("📢 Room event sent to user " + participant.getId() + ": room-status-changed");
            });
            
            System.out.println("✅ Room status changed event sent successfully");
            
        } catch (Exception e) {
            System.err.println("❌ Failed to send room status changed event: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Send room capacity changed event
    public void sendRoomCapacityChangedEvent(CallRoom room, int oldCapacity, int newCapacity) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("type", "room-capacity-changed");
            event.put("roomId", room.getRoomId());
            event.put("oldCapacity", oldCapacity);
            event.put("newCapacity", newCapacity);
            event.put("timestamp", System.currentTimeMillis());
            event.put("participantCount", room.getParticipants().size());
            event.put("maxParticipants", room.getMaxParticipants());
            
            // Send to all room participants
            room.getParticipants().forEach(participant -> {
                String destination = "/user/" + participant.getId() + "/queue/room-events";
                messagingTemplate.convertAndSend(destination, event);
                System.out.println("📢 Room event sent to user " + participant.getId() + ": room-capacity-changed");
            });
            
            System.out.println("✅ Room capacity changed event sent successfully");
            
        } catch (Exception e) {
            System.err.println("❌ Failed to send room capacity changed event: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Send room ended event
    public void sendRoomEndedEvent(CallRoom room, User endedBy) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("type", "room-ended");
            event.put("roomId", room.getRoomId());
            event.put("endedBy", endedBy.getId());
            event.put("endedByName", endedBy.getFname() + " " + endedBy.getLname());
            event.put("timestamp", System.currentTimeMillis());
            event.put("participantCount", room.getParticipants().size());
            
            // Send to all room participants
            room.getParticipants().forEach(participant -> {
                String destination = "/user/" + participant.getId() + "/queue/room-events";
                messagingTemplate.convertAndSend(destination, event);
                System.out.println("📢 Room event sent to user " + participant.getId() + ": room-ended");
            });
            
            System.out.println("✅ Room ended event sent successfully");
            
        } catch (Exception e) {
            System.err.println("❌ Failed to send room ended event: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Send room participant list update
    public void sendParticipantListUpdate(CallRoom room) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("type", "participant-list-update");
            event.put("roomId", room.getRoomId());
            event.put("timestamp", System.currentTimeMillis());
            event.put("participantCount", room.getParticipants().size());
            event.put("maxParticipants", room.getMaxParticipants());
            
            // Create participant list
            Map<String, Object> participants = new HashMap<>();
            room.getParticipants().forEach(participant -> {
                participants.put(participant.getId().toString(), Map.of(
                    "id", participant.getId(),
                    "name", participant.getFname() + " " + participant.getLname(),
                    "email", participant.getEmail()
                ));
            });
            event.put("participants", participants);
            
            // Send to all room participants
            room.getParticipants().forEach(participant -> {
                String destination = "/user/" + participant.getId() + "/queue/room-events";
                messagingTemplate.convertAndSend(destination, event);
                System.out.println("📢 Room event sent to user " + participant.getId() + ": participant-list-update");
            });
            
            System.out.println("✅ Participant list update sent successfully");
            
        } catch (Exception e) {
            System.err.println("❌ Failed to send participant list update: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Send custom room event
    public void sendCustomRoomEvent(CallRoom room, String eventType, Map<String, Object> eventData) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("type", eventType);
            event.put("roomId", room.getRoomId());
            event.put("timestamp", System.currentTimeMillis());
            event.putAll(eventData);
            
            // Send to all room participants
            room.getParticipants().forEach(participant -> {
                String destination = "/user/" + participant.getId() + "/queue/room-events";
                messagingTemplate.convertAndSend(destination, event);
                System.out.println("📢 Room event sent to user " + participant.getId() + ": " + eventType);
            });
            
            System.out.println("✅ Custom room event sent successfully: " + eventType);
            
        } catch (Exception e) {
            System.err.println("❌ Failed to send custom room event: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
