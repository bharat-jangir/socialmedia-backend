package com.bharat.springbootsocial.response;

import com.bharat.springbootsocial.entity.CallParticipant;
import com.bharat.springbootsocial.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CallParticipantResponse {
    
    private UUID id;
    private User user;
    private CallParticipant.ParticipantStatus status;
    private LocalDateTime joinedAt;
    private LocalDateTime leftAt;
    private Boolean isMuted;
    private Boolean isVideoEnabled;
    private Boolean isSpeaking;
    private String connectionQuality;
}
