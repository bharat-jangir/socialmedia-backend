package com.bharat.springbootsocial.services;

import com.bharat.springbootsocial.entity.CallRoom;
import com.bharat.springbootsocial.entity.User;
import com.bharat.springbootsocial.exception.UserException;

import java.util.List;
import java.util.UUID;

public interface CallRoomService {
    
    // Create a new call room
    CallRoom createCallRoom(User creator, String roomName, CallRoom.CallType callType, List<UUID> participantIds) throws UserException;
    
    // Join an existing call room
    CallRoom joinCallRoom(String roomId, User user) throws UserException;
    
    // Leave a call room
    void leaveCallRoom(String roomId, User user) throws UserException;
    
    // End a call room
    void endCallRoom(String roomId, User user) throws UserException;
    
    // Get call room by ID
    CallRoom getCallRoomById(String roomId) throws UserException;
    
    // Get active rooms for user
    List<CallRoom> getActiveRoomsForUser(User user);
    
    // Get rooms created by user
    List<CallRoom> getRoomsCreatedByUser(User user);
    
    // Add participant to room
    CallRoom addParticipant(String roomId, User user, UUID participantId) throws UserException;
    
    // Remove participant from room
    CallRoom removeParticipant(String roomId, User user, UUID participantId) throws UserException;
    
    // Update room status
    CallRoom updateRoomStatus(String roomId, CallRoom.CallStatus status) throws UserException;
    
    // Check if user can join room
    boolean canUserJoinRoom(String roomId, User user) throws UserException;
    
    // Get room participants
    List<User> getRoomParticipants(String roomId) throws UserException;
    
    // Cleanup old rooms
    void cleanupOldRooms();
    
    // Get room statistics
    CallRoomStats getRoomStatistics(String roomId) throws UserException;
    
    // DTO for room statistics
    class CallRoomStats {
        private String roomId;
        private int participantCount;
        private long durationSeconds;
        private CallRoom.CallStatus status;
        private boolean isActive;
        
        // Constructors, getters, setters
        public CallRoomStats() {}
        
        public CallRoomStats(String roomId, int participantCount, long durationSeconds, 
                           CallRoom.CallStatus status, boolean isActive) {
            this.roomId = roomId;
            this.participantCount = participantCount;
            this.durationSeconds = durationSeconds;
            this.status = status;
            this.isActive = isActive;
        }
        
        // Getters and setters
        public String getRoomId() { return roomId; }
        public void setRoomId(String roomId) { this.roomId = roomId; }
        
        public int getParticipantCount() { return participantCount; }
        public void setParticipantCount(int participantCount) { this.participantCount = participantCount; }
        
        public long getDurationSeconds() { return durationSeconds; }
        public void setDurationSeconds(long durationSeconds) { this.durationSeconds = durationSeconds; }
        
        public CallRoom.CallStatus getStatus() { return status; }
        public void setStatus(CallRoom.CallStatus status) { this.status = status; }
        
        public boolean isActive() { return isActive; }
        public void setActive(boolean active) { isActive = active; }
    }
}