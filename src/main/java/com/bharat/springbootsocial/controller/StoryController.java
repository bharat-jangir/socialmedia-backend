package com.bharat.springbootsocial.controller;

import com.bharat.springbootsocial.entity.User;
import java.util.UUID;
import com.bharat.springbootsocial.request.CreateStoryRequest;
import com.bharat.springbootsocial.request.StoryReplyRequest;
import com.bharat.springbootsocial.response.ApiResponse;
import com.bharat.springbootsocial.response.PaginatedStoryResponse;
import com.bharat.springbootsocial.response.StoryResponse;
import com.bharat.springbootsocial.response.UserStoryResponse;
import com.bharat.springbootsocial.services.ServiceInt;
import com.bharat.springbootsocial.services.StoryService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stories")
@AllArgsConstructor
@CrossOrigin(origins = "*")
public class StoryController {
    
    @Autowired
    private StoryService storyService;
    
    @Autowired
    private ServiceInt userService;

    // Create a new story
    @PostMapping
    public ResponseEntity<StoryResponse> createStory(
            @RequestBody CreateStoryRequest request,
            @RequestHeader("Authorization") String jwt) {
        User reqUser = userService.getUserFromToken(jwt);
        StoryResponse story = storyService.createStory(request, reqUser);
        return new ResponseEntity<>(story, HttpStatus.CREATED);
    }
    
    // Get a specific story by ID
    @GetMapping("/{storyId}")
    public ResponseEntity<StoryResponse> getStoryById(
            @PathVariable UUID storyId,
            @RequestHeader("Authorization") String jwt) {
        try {
            User reqUser = userService.getUserFromToken(jwt);
            StoryResponse story = storyService.getStoryById(storyId, reqUser);
            return new ResponseEntity<>(story, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    // Get active stories by user ID
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<StoryResponse>> getActiveStoriesByUserId(
            @PathVariable UUID userId,
            @RequestHeader("Authorization") String jwt) {
        User reqUser = userService.getUserFromToken(jwt);
        List<StoryResponse> stories = storyService.getActiveStoriesByUserId(userId, reqUser);
        return new ResponseEntity<>(stories, HttpStatus.OK);
    }
    
    // Get stories from users that the current user follows (Instagram-like feed)
    @GetMapping("/following")
    public ResponseEntity<List<UserStoryResponse>> getStoriesFromFollowing(
            @RequestHeader("Authorization") String jwt) {
        User reqUser = userService.getUserFromToken(jwt);
        List<UserStoryResponse> stories = storyService.getStoriesFromFollowing(reqUser);
        return new ResponseEntity<>(stories, HttpStatus.OK);
    }
    
    // Get paginated stories from users that the current user follows
    @GetMapping("/following/paginated")
    public ResponseEntity<PaginatedStoryResponse> getStoriesFromFollowingPaginated(
            @RequestHeader("Authorization") String jwt,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        User reqUser = userService.getUserFromToken(jwt);
        Pageable pageable = PageRequest.of(page, size);
        PaginatedStoryResponse stories = storyService.getStoriesFromFollowingPaginated(reqUser, pageable);
        return new ResponseEntity<>(stories, HttpStatus.OK);
    }
    
    // Delete a story
    @DeleteMapping("/{storyId}")
    public ResponseEntity<ApiResponse> deleteStory(
            @PathVariable UUID storyId,
            @RequestHeader("Authorization") String jwt) {
        try {
            User reqUser = userService.getUserFromToken(jwt);
            storyService.deleteStory(storyId, reqUser);
            ApiResponse response = new ApiResponse("Story deleted successfully", true);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    // View a story (increments view count)
    @PostMapping("/{storyId}/view")
    public ResponseEntity<StoryResponse> viewStory(
            @PathVariable UUID storyId,
            @RequestHeader("Authorization") String jwt) {
        try {
            User reqUser = userService.getUserFromToken(jwt);
            StoryResponse story = storyService.viewStory(storyId, reqUser);
            return new ResponseEntity<>(story, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    // Like a story
    @PostMapping("/{storyId}/like")
    public ResponseEntity<StoryResponse> likeStory(
            @PathVariable UUID storyId,
            @RequestHeader("Authorization") String jwt) {
        try {
            User reqUser = userService.getUserFromToken(jwt);
            StoryResponse story = storyService.likeStory(storyId, reqUser);
            return new ResponseEntity<>(story, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    // Unlike a story
    @DeleteMapping("/{storyId}/like")
    public ResponseEntity<StoryResponse> unlikeStory(
            @PathVariable UUID storyId,
            @RequestHeader("Authorization") String jwt) {
        try {
            User reqUser = userService.getUserFromToken(jwt);
            StoryResponse story = storyService.unlikeStory(storyId, reqUser);
            return new ResponseEntity<>(story, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    // Reply to a story
    @PostMapping("/reply")
    public ResponseEntity<StoryResponse> replyToStory(
            @RequestBody StoryReplyRequest request,
            @RequestHeader("Authorization") String jwt) {
        try {
            User reqUser = userService.getUserFromToken(jwt);
            StoryResponse story = storyService.replyToStory(request, reqUser);
            return new ResponseEntity<>(story, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    // Get unread replies for the current user's stories
    @GetMapping("/replies/unread")
    public ResponseEntity<List<StoryResponse>> getUnreadReplies(
            @RequestHeader("Authorization") String jwt) {
        User reqUser = userService.getUserFromToken(jwt);
        List<StoryResponse> stories = storyService.getUnreadReplies(reqUser);
        return new ResponseEntity<>(stories, HttpStatus.OK);
    }
    
    // Mark a reply as read
    @PutMapping("/replies/{replyId}/read")
    public ResponseEntity<ApiResponse> markReplyAsRead(
            @PathVariable UUID replyId,
            @RequestHeader("Authorization") String jwt) {
        try {
            User reqUser = userService.getUserFromToken(jwt);
            storyService.markReplyAsRead(replyId, reqUser);
            ApiResponse response = new ApiResponse("Reply marked as read", true);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    // Get story analytics
    @GetMapping("/{storyId}/analytics")
    public ResponseEntity<ApiResponse> getStoryAnalytics(
            @PathVariable UUID storyId,
            @RequestHeader("Authorization") String jwt) {
        try {
            User reqUser = userService.getUserFromToken(jwt);
            
            Integer viewCount = storyService.getStoryViewCount(storyId);
            Integer likeCount = storyService.getStoryLikeCount(storyId);
            Integer replyCount = storyService.getStoryReplyCount(storyId);
            
            ApiResponse response = new ApiResponse();
            response.setMessage("Story analytics retrieved successfully");
            response.setStatus(true);
            
            // Create analytics data object
            Object analyticsData = new Object() {
                public final Integer views = viewCount;
                public final Integer likes = likeCount;
                public final Integer replies = replyCount;
            };
            response.setData(analyticsData);
            
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
