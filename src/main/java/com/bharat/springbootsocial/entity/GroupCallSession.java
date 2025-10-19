package com.bharat.springbootsocial.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "group_call_sessions", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"group_call_room_id", "user_id"}))
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class GroupCallSession {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "session_id", unique = true, nullable = false)
    private String sessionId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_call_room_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private GroupCallRoom groupCallRoom;
    
    @Column(name = "group_call_room_id", nullable = false, insertable = false, updatable = false)
    private UUID groupCallRoomId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User user;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SessionStatus status;
    
    @Column(name = "joined_at")
    private LocalDateTime joinedAt;
    
    @Column(name = "left_at")
    private LocalDateTime leftAt;
    
    @Column(name = "is_muted")
    private Boolean isMuted = false;
    
    @Column(name = "is_video_enabled")
    private Boolean isVideoEnabled = true;
    
    @Column(name = "is_speaking")
    private Boolean isSpeaking = false;
    
    // WebRTC connection state
    @Column(name = "connection_state")
    private String connectionState = "new";
    
    @Column(name = "ice_connection_state")
    private String iceConnectionState = "new";
    
    @Column(name = "last_activity")
    private LocalDateTime lastActivity;
    
    public enum SessionStatus {
        JOINING,
        CONNECTED,
        DISCONNECTED,
        FAILED
    }
    
    @PrePersist
    protected void onCreate() {
        joinedAt = LocalDateTime.now();
        lastActivity = LocalDateTime.now();
        if (status == null) {
            status = SessionStatus.JOINING;
        }
        if (sessionId == null) {
            sessionId = generateSessionId();
        }
        // groupCallRoomId will be automatically set by the @JoinColumn relationship
    }
    
    private String generateSessionId() {
        // Use more unique generation with UUID and timestamp
        return "group_session_" + System.currentTimeMillis() + "_" + 
               java.util.UUID.randomUUID().toString().substring(0, 8);
    }
    
    public void updateActivity() {
        this.lastActivity = LocalDateTime.now();
    }
    
    public void leaveSession() {
        this.status = SessionStatus.DISCONNECTED;
        this.leftAt = LocalDateTime.now();
    }
    
    public void setConnectionState(String state) {
        this.connectionState = state;
        updateActivity();
    }
    
    public void setIceConnectionState(String state) {
        this.iceConnectionState = state;
        updateActivity();
    }
}
