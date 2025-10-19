package com.bharat.springbootsocial.request;

import com.bharat.springbootsocial.entity.CallRoom;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateCallRoomRequest {
    
    private String roomName;
    private CallRoom.CallType callType;
    private Integer maxParticipants = 10;
    private Boolean isPrivate = false;
    private List<Long> participantIds; // Users to invite
}
