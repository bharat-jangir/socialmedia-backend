package com.bharat.springbootsocial.response;

import com.bharat.springbootsocial.entity.Post;
import com.bharat.springbootsocial.entity.Reels;
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
public class ContentResponse {
    private UUID id;
    private String type; // "post" or "reel"
    private String caption; // For posts
    private String title; // For reels
    private String image; // For posts
    private String video; // For both posts and reels
    private User user;
    
    // Only latest 3 users who liked the content (from following/followers)
    private List<User> recentLikedBy;
    
    // Only latest 3 comments
    private List<CommentResponse> recentComments;
    
    // Total counts for UI management
    private Integer totalLikes;
    private Integer totalComments;
    
    private LocalDateTime createdAt;
    
    // Constructor for Post
    public ContentResponse(Post post, List<User> recentLikedBy, List<CommentResponse> recentComments) {
        this.id = post.getId();
        this.type = "post";
        this.caption = post.getCaption();
        this.image = post.getImage();
        this.video = post.getVideo();
        this.user = post.getUser();
        this.recentLikedBy = recentLikedBy;
        this.recentComments = recentComments;
        this.totalLikes = post.getLikedBy() != null ? post.getLikedBy().size() : 0;
        this.totalComments = post.getComments() != null ? post.getComments().size() : 0;
        this.createdAt = post.getCreatedAt();
    }
    
    // Constructor for Reels
    public ContentResponse(Reels reel, List<User> recentLikedBy, List<CommentResponse> recentComments) {
        this.id = reel.getId();
        this.type = "reel";
        this.title = reel.getTitle();
        this.video = reel.getVideo();
        this.user = reel.getUser();
        this.recentLikedBy = recentLikedBy;
        this.recentComments = recentComments;
        this.totalLikes = reel.getLikedBy() != null ? reel.getLikedBy().size() : 0;
        this.totalComments = reel.getComments() != null ? reel.getComments().size() : 0;
        this.createdAt = reel.getCreatedAt();
    }
    
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
