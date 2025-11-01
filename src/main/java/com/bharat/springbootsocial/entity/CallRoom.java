package com.bharat.springbootsocial.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "call_rooms")
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class CallRoom {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;
    
    @Column(unique = true, nullable = false)
    private String roomId;
    
    // Ensure roomId is always available for JSON serialization
    public String getRoomId() {
        if (roomId == null) {
            roomId = generateRoomId();
        }
        return roomId;
    }
    
    @Column(nullable = false)
    private String roomName;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false, columnDefinition = "BINARY(16)")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User createdBy;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CallType callType;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CallStatus status;
    
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "call_room_participants",
        joinColumns = @JoinColumn(name = "room_id", columnDefinition = "BINARY(16)"),
        inverseJoinColumns = @JoinColumn(name = "user_id", columnDefinition = "BINARY(16)")
    )
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private List<User> participants = new ArrayList<>();
    
    @Column(name = "max_participants")
    private Integer maxParticipants = 10; // Default for group calls
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "ended_at")
    private LocalDateTime endedAt;
    
    // Call statistics
    @Column(name = "duration_seconds")
    private Long durationSeconds = 0L;
    
    public enum CallType {
        VOICE_ONLY,
        VIDEO_CALL,
        SCREEN_SHARE
    }
    
    public enum CallStatus {
        WAITING,        // Room created, waiting for participants
        ACTIVE,         // Call in progress
        ENDED,          // Call ended
        CANCELLED       // Call cancelled
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (roomId == null) {
            roomId = generateRoomId();
        }
    }
    
    private String generateRoomId() {
        return "room_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
    }
    
    public void addParticipant(User user) {
        if (!participants.contains(user)) {
            participants.add(user);
        }
    }
    
    public void removeParticipant(User user) {
        participants.remove(user);
    }
    
    public boolean isParticipant(User user) {
        return participants.contains(user);
    }
    
    public boolean canJoin() {
        return isActive && (status == CallStatus.WAITING || status == CallStatus.ACTIVE) && 
               participants.size() < maxParticipants;
    }
    
    public void endCall() {
        this.status = CallStatus.ENDED;
        this.isActive = false;
        this.endedAt = LocalDateTime.now();
        if (createdAt != null) {
            this.durationSeconds = java.time.Duration.between(createdAt, endedAt).getSeconds();
        }
    }
}
