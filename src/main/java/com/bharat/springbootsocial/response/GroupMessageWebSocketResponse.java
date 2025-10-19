package com.bharat.springbootsocial.response;

import com.bharat.springbootsocial.entity.GroupMessage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupMessageWebSocketResponse {
    private UUID messageId;
    private UUID groupId;
    private String content;
    private GroupMessage.MessageType messageType;
    private String mediaUrl;
    private String mediaType;
    private UUID replyToMessageId;
    private UserResponse sender;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String action; // "message_sent", "message_edited", "message_deleted", "reaction_added", "reaction_removed", "typing", "stop_typing"
    private List<MessageReactionResponse> reactions;
    private List<MessageReadResponse> readBy;
    private boolean isEdited;
    private boolean isDeleted;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserResponse {
        private UUID id;
        private String firstName;
        private String lastName;
        private String email;
        private String profileImage;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MessageReactionResponse {
        private UUID userId;
        private String reaction;
        private LocalDateTime createdAt;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MessageReadResponse {
        private UUID userId;
        private LocalDateTime readAt;
    }
}
