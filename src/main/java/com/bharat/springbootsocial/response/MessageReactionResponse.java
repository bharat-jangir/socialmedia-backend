package com.bharat.springbootsocial.response;

import com.bharat.springbootsocial.entity.MessageReaction;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageReactionResponse {
    
    private UUID id;
    private UUID messageId;
    private UUID userId;
    private String userName;
    private String userProfileImage;
    private String emoji;
    private LocalDateTime createdAt;
    
    public static MessageReactionResponse fromEntity(MessageReaction reaction) {
        MessageReactionResponse response = new MessageReactionResponse();
        response.setId(reaction.getId());
        response.setMessageId(reaction.getMessage().getId());
        response.setUserId(reaction.getUser().getId());
        response.setUserName(reaction.getUser().getFname() + " " + reaction.getUser().getLname());
        response.setUserProfileImage(reaction.getUser().getProfileImage());
        response.setEmoji(reaction.getEmoji());
        response.setCreatedAt(reaction.getCreatedAt());
        return response;
    }
}
