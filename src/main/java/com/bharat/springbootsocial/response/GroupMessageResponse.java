package com.bharat.springbootsocial.response;

import com.bharat.springbootsocial.entity.GroupMessage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupMessageResponse {
    private UUID id;
    private String content;
    private GroupMessage.MessageType messageType;
    private String imageUrl;
    private String videoUrl;
    private String fileUrl;
    private String fileName;
    private Long fileSize;
    private UserResponse sender;
    private GroupMessageResponse replyTo;
    private Boolean isEdited;
    private LocalDateTime editedAt;
    private Boolean isDeleted;
    private LocalDateTime deletedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<ReactionResponse> reactions;
    private List<ReadResponse> readBy;
    private Boolean systemMessage;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserResponse {
        private UUID id;
        private String fname;
        private String lname;
        private String email;
        private String gender;
        private String profileImage;
        private String coverImage;
        private String userBio;
        private List<UUID> following;
        private List<UUID> followers;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReactionResponse {
        private UUID id;
        private String emoji;
        private UserResponse user;
        private LocalDateTime createdAt;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReadResponse {
        private UUID id;
        private UserResponse user;
        private LocalDateTime readAt;
    }
    
    // Constructor to convert from GroupMessage entity
    public GroupMessageResponse(GroupMessage message) {
        this.id = message.getId();
        this.content = message.getContent();
        this.messageType = message.getMessageType();
        this.imageUrl = message.getImageUrl();
        this.videoUrl = message.getVideoUrl();
        this.fileUrl = message.getFileUrl();
        this.fileName = message.getFileName();
        this.fileSize = message.getFileSize();
        this.sender = convertToUserResponse(message.getSender());
        this.replyTo = message.getReplyTo() != null ? new GroupMessageResponse(message.getReplyTo()) : null;
        this.isEdited = message.getIsEdited();
        this.editedAt = message.getEditedAt();
        this.isDeleted = message.getIsDeleted();
        this.deletedAt = message.getDeletedAt();
        this.createdAt = message.getCreatedAt();
        this.updatedAt = message.getUpdatedAt();
        this.systemMessage = message.isSystemMessage();
        
        // Convert reactions
        if (message.getReactions() != null && !message.getReactions().isEmpty()) {
            this.reactions = message.getReactions().stream()
                .map(reaction -> new ReactionResponse(
                    reaction.getId(),
                    reaction.getEmoji(),
                    convertToUserResponse(reaction.getUser()),
                    reaction.getCreatedAt()
                ))
                .collect(Collectors.toList());
        } else {
            this.reactions = List.of();
        }
        
        // Convert read receipts
        if (message.getReadBy() != null && !message.getReadBy().isEmpty()) {
            this.readBy = message.getReadBy().stream()
                .map(read -> new ReadResponse(
                    read.getId(),
                    convertToUserResponse(read.getUser()),
                    read.getReadAt()
                ))
                .collect(Collectors.toList());
        }
    }
    
    private UserResponse convertToUserResponse(com.bharat.springbootsocial.entity.User user) {
        if (user == null) return null;
        
        UserResponse userResponse = new UserResponse();
        userResponse.setId(user.getId());
        userResponse.setFname(user.getFname());
        userResponse.setLname(user.getLname());
        userResponse.setEmail(user.getEmail());
        userResponse.setGender(user.getGender());
        userResponse.setProfileImage(user.getProfileImage());
        userResponse.setCoverImage(user.getCoverImage());
        userResponse.setUserBio(user.getUserBio());
        userResponse.setFollowing(user.getFollowing());
        userResponse.setFollowers(user.getFollowers());
        return userResponse;
    }
    
    // Helper method to get reaction counts by emoji
    public Map<String, Long> getReactionCounts() {
        if (reactions == null || reactions.isEmpty()) {
            return Map.of();
        }
        
        return reactions.stream()
            .collect(Collectors.groupingBy(
                ReactionResponse::getEmoji,
                Collectors.counting()
            ));
    }
    
    // Helper method to get total reaction count
    public int getTotalReactionCount() {
        return reactions != null ? reactions.size() : 0;
    }
    
    // Helper method to get read count
    public int getReadCount() {
        return readBy != null ? readBy.size() : 0;
    }
    
    // Helper method to check if user has reacted with specific emoji
    public boolean hasUserReactedWithEmoji(UUID userId, String emoji) {
        if (reactions == null || reactions.isEmpty()) {
            return false;
        }
        
        return reactions.stream()
            .anyMatch(reaction -> 
                reaction.getUser().getId().equals(userId) && 
                reaction.getEmoji().equals(emoji)
            );
    }
    
    // Helper method to get user's reactions
    public List<String> getUserReactions(UUID userId) {
        if (reactions == null || reactions.isEmpty()) {
            return List.of();
        }
        
        return reactions.stream()
            .filter(reaction -> reaction.getUser().getId().equals(userId))
            .map(ReactionResponse::getEmoji)
            .collect(Collectors.toList());
    }
}
