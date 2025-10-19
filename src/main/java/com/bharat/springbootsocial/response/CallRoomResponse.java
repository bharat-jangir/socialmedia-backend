package com.bharat.springbootsocial.response;

import com.bharat.springbootsocial.entity.CallRoom;
import com.bharat.springbootsocial.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CallRoomResponse {
    
    private UUID id;
    private String roomId;
    private String roomName;
    private User createdBy;
    private CallRoom.CallType callType;
    private CallRoom.CallStatus status;
    private Integer maxParticipants;
    private Boolean isPrivate;
    private LocalDateTime createdAt;
    private LocalDateTime endedAt;
    private List<CallParticipantResponse> participants;
    private Integer activeParticipantsCount;
}
