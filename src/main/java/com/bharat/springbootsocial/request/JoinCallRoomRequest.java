package com.bharat.springbootsocial.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JoinCallRoomRequest {
    
    private String roomId;
    private String sessionId;
    private Boolean enableVideo = true;
    private Boolean enableAudio = true;
}
