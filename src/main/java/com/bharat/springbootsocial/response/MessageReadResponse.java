package com.bharat.springbootsocial.response;

import com.bharat.springbootsocial.entity.MessageRead;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageReadResponse {
    
    private UUID id;
    private UUID messageId;
    private UUID userId;
    private String userName;
    private String userProfileImage;
    private LocalDateTime readAt;
    
    public static MessageReadResponse fromEntity(MessageRead messageRead) {
        MessageReadResponse response = new MessageReadResponse();
        response.setId(messageRead.getId());
        response.setMessageId(messageRead.getMessage().getId());
        response.setUserId(messageRead.getUser().getId());
        response.setUserName(messageRead.getUser().getFname() + " " + messageRead.getUser().getLname());
        response.setUserProfileImage(messageRead.getUser().getProfileImage());
        response.setReadAt(messageRead.getReadAt());
        return response;
    }
}
