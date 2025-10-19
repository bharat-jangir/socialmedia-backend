package com.bharat.springbootsocial.response;

import com.bharat.springbootsocial.entity.User;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class PostResponse {
    private UUID id;
    private String caption;
    private String image;
    private String video;
    private User user;
    
    // Only latest 3 users who liked the post (from following/followers)
    private List<User> recentLikedBy;
    
    // Only latest 3 comments
    private List<CommentResponse> recentComments;
    
    // Total counts for UI management
    private Integer totalLikes;
    private Integer totalComments;
    
    private LocalDateTime createdAt;
    
    // Nested CommentResponse class for optimized comment structure
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    public static class CommentResponse {
        private UUID id;
        private String content;
        private User user;
        private Integer totalLikes;
        private Boolean isLiked;
        private LocalDateTime createdAt;
    }
}
