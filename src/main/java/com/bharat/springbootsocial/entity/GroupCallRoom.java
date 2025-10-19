package com.bharat.springbootsocial.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
@Table(name = "user_group_call_rooms")
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class GroupCallRoom {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(unique = true, nullable = false)
    private String roomId;
    
    @Column(nullable = false)
    private String roomName;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    @JsonIgnore
    private Group group;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User createdBy;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CallType callType;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CallStatus status;
    
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "user_group_call_participants",
        joinColumns = @JoinColumn(name = "call_room_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private List<User> participants = new ArrayList<>();
    
    @Column(name = "max_participants")
    private Integer maxParticipants = 50; // Default for group calls
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "ended_at")
    private LocalDateTime endedAt;
    
    @Column(name = "duration_seconds")
    private Long durationSeconds = 0L;
    
    @Column(name = "is_scheduled")
    private Boolean isScheduled = false;
    
    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;
    
    @Column(name = "description")
    private String description;
    
    public enum CallType {
        VOICE_ONLY,
        VIDEO_CALL,
        SCREEN_SHARE
    }
    
    public enum CallStatus {
        WAITING,        // Room created, waiting for participants
        ACTIVE,         // Call in progress
        ENDED,          // Call ended
        CANCELLED,      // Call cancelled
        SCHEDULED       // Call is scheduled
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (roomId == null) {
            roomId = generateRoomId();
        }
    }
    
    private String generateRoomId() {
        return "group_call_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
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
    
    public void startCall() {
        this.status = CallStatus.ACTIVE;
    }
    
    public boolean isGroupMember(User user) {
        return group.isMember(user);
    }
}
