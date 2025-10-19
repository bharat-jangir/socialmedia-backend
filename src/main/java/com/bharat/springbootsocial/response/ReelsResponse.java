package com.bharat.springbootsocial.response;

import com.bharat.springbootsocial.entity.Reels;
import com.bharat.springbootsocial.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReelsResponse {
    private UUID id;
    private String title;
    private String video;
    private User user;
    private List<UUID> likedBy; // Only user IDs, not full user objects
    private LocalDateTime createdAt;
    
    // Additional fields for counts
    private int totalLikes;
    private int totalComments;
    
    // Constructor to create ReelsResponse from Reels entity
    public ReelsResponse(Reels reel) {
        this.id = reel.getId();
        this.title = reel.getTitle();
        this.video = reel.getVideo();
        this.user = reel.getUser();
        // Extract only user IDs from likedBy
        this.likedBy = reel.getLikedBy() != null ? 
            reel.getLikedBy().stream().map(User::getId).toList() : 
            List.of();
        this.createdAt = reel.getCreatedAt();
        this.totalLikes = reel.getLikedBy() != null ? reel.getLikedBy().size() : 0;
        this.totalComments = reel.getComments() != null ? reel.getComments().size() : 0;
    }
}
