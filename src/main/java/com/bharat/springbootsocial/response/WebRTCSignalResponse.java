package com.bharat.springbootsocial.response;

import com.bharat.springbootsocial.entity.WebRTCSignal;
import com.bharat.springbootsocial.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WebRTCSignalResponse {
    
    private UUID id;
    private String roomId;
    private User fromUser;
    private User toUser;
    private WebRTCSignal.SignalType signalType;
    private String signalData;
    private String sessionId;
    private LocalDateTime createdAt;
    private Boolean isProcessed;
}
