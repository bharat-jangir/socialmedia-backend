package com.bharat.springbootsocial.request;

import com.bharat.springbootsocial.entity.WebRTCSignal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WebRTCSignalRequest {
    
    private String roomId;
    private UUID toUserId;
    private WebRTCSignal.SignalType signalType;
    private String signalData;
    private String sessionId;
}
