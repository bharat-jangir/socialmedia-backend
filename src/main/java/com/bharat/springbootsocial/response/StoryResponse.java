package com.bharat.springbootsocial.response;

import com.bharat.springbootsocial.entity.Story;
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
public class StoryResponse {
    private UUID id;
    private String imageUrl;
    private String videoUrl;
    private String caption;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private Boolean isActive;
    private Story.StoryType storyType;
    private Integer viewCount;
    private Integer likeCount;
    private Integer replyCount;
    private Boolean isViewed;
    private Boolean isLiked;
    private List<StoryReplyResponse> replies;
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class StoryReplyResponse {
        private UUID id;
        private User user;
        private String replyText;
        private LocalDateTime createdAt;
        private Boolean isRead;
    }
}
