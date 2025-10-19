package com.bharat.springbootsocial.response;

import com.bharat.springbootsocial.entity.Message;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EnhancedMessageResponse {
    
    private UUID id;
    private String content;
    private String imageUrl;
    private String videoUrl;
    private String fileUrl;
    private String fileName;
    private Long fileSize;
    private Message.MessageType messageType;
    private UUID chatId;
    private UUID replyToId;
    private String replyToContent;
    private Boolean isEdited;
    private LocalDateTime editedAt;
    private Boolean isDeleted;
    private LocalDateTime deletedAt;
    private LocalDateTime timestamp;
    private LocalDateTime updatedAt;
    
    // User information
    private UUID userId;
    private String userName;
    private String userProfileImage;
    
    // Reactions and reads
    private List<MessageReactionResponse> reactions;
    private List<MessageReadResponse> readBy;
    private int reactionCount;
    private int readCount;
    
    public static EnhancedMessageResponse fromEntity(Message message) {
        EnhancedMessageResponse response = new EnhancedMessageResponse();
        response.setId(message.getId());
        response.setContent(message.getContent());
        response.setImageUrl(message.getImage());
        response.setVideoUrl(message.getVideoUrl());
        response.setFileUrl(message.getFileUrl());
        response.setFileName(message.getFileName());
        response.setFileSize(message.getFileSize());
        response.setMessageType(message.getMessageType());
        response.setChatId(message.getChat().getId());
        response.setReplyToId(message.getReplyTo() != null ? message.getReplyTo().getId() : null);
        response.setReplyToContent(message.getReplyTo() != null ? message.getReplyTo().getContent() : null);
        response.setIsEdited(message.getIsEdited());
        response.setEditedAt(message.getEditedAt());
        response.setIsDeleted(message.getIsDeleted());
        response.setDeletedAt(message.getDeletedAt());
        response.setTimestamp(message.getTimestamp());
        response.setUpdatedAt(message.getUpdatedAt());
        
        // User information
        response.setUserId(message.getUser().getId());
        response.setUserName(message.getUser().getFname() + " " + message.getUser().getLname());
        response.setUserProfileImage(message.getUser().getProfileImage());
        
        // Reactions and reads
        response.setReactions(message.getReactions().stream()
                .map(MessageReactionResponse::fromEntity)
                .collect(Collectors.toList()));
        response.setReadBy(message.getReadBy().stream()
                .map(MessageReadResponse::fromEntity)
                .collect(Collectors.toList()));
        response.setReactionCount(message.getReactionCount());
        response.setReadCount(message.getReadCount());
        
        return response;
    }
}
