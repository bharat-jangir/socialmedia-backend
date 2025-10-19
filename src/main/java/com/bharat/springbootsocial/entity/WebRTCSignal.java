package com.bharat.springbootsocial.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "webrtc_signals")
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class WebRTCSignal {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "room_id", nullable = false)
    private String roomId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_user_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User fromUser;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_user_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User toUser;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SignalType signalType;
    
    @Column(name = "signal_data", columnDefinition = "TEXT")
    private String signalData;
    
    @Column(name = "session_id")
    private String sessionId;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "processed_at")
    private LocalDateTime processedAt;
    
    @Column(name = "is_processed")
    private Boolean isProcessed = false;
    
    public enum SignalType {
        OFFER, ANSWER, ICE_CANDIDATE, CALL_INITIATED, CALL_ACCEPTED, CALL_DECLINED, CALL_ENDED, 
        USER_JOINED, USER_LEFT, MUTE_TOGGLE, VIDEO_TOGGLE, SPEAKING_STATUS
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
