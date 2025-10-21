package com.bharat.springbootsocial.controller;

import com.bharat.springbootsocial.entity.Comment;
import com.bharat.springbootsocial.entity.Reels;
import com.bharat.springbootsocial.entity.User;
import java.util.UUID;
import com.bharat.springbootsocial.request.CommentRequest;
import com.bharat.springbootsocial.response.ApiResponse;
import com.bharat.springbootsocial.response.CommentResponse;
import com.bharat.springbootsocial.response.PaginatedResponse;
import com.bharat.springbootsocial.response.ReelsResponse;
import com.bharat.springbootsocial.services.ReelsService;
import com.bharat.springbootsocial.services.ServiceInt;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/reels")
@AllArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "https://socialmedia-frontend-l78j.onrender.com"})
public class ReelsController {
    @Autowired
    private ReelsService reelsService;
    @Autowired
    private ServiceInt userService;

    @PostMapping()
    public Reels createReels(@RequestHeader("Authorization") String jwt, @RequestBody Reels reels) {
        User reqUser = userService.getUserFromToken(jwt);
        return reelsService.createReel(reels, reqUser);
    }

    @DeleteMapping("/{reelId}")
    public ResponseEntity<ApiResponse> deleteReel(@RequestHeader("Authorization") String jwt, @PathVariable UUID reelId)
            throws Exception {
        User reqUser = userService.getUserFromToken(jwt);
        String message = reelsService.deleteReel(reelId, reqUser.getId());
        ApiResponse response = new ApiResponse(message, true);
        return new ResponseEntity<ApiResponse>(response, HttpStatus.OK);
    }

    @GetMapping()
    public List<ReelsResponse> getAllReels() {
        return reelsService.getAllReelsWithCounts();
    }

    @GetMapping("/user/{userId}")
    public List<ReelsResponse> getUserReels(@PathVariable("userId") UUID userId) {
        return reelsService.getReelsByUserIdWithCounts(userId);
    }
    
    // Infinite scroll endpoints
    @GetMapping("/feed")
    public ResponseEntity<PaginatedResponse<ReelsResponse>> getAllReelsFeed(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PaginatedResponse<ReelsResponse> reels = reelsService.getAllReelsPaginatedWithCounts(page, size);
        return new ResponseEntity<>(reels, HttpStatus.OK);
    }
    
    @GetMapping("/user/{userId}/feed")
    public ResponseEntity<PaginatedResponse<ReelsResponse>> getUserReelsFeed(
            @PathVariable("userId") UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PaginatedResponse<ReelsResponse> reels = reelsService.getReelsByUserIdPaginatedWithCounts(userId, page, size);
        return new ResponseEntity<>(reels, HttpStatus.OK);
    }
    
    // Like/Unlike endpoints
    @GetMapping("/{reelId}")
    public ResponseEntity<ReelsResponse> findReelById(@PathVariable UUID reelId) throws Exception {
        ReelsResponse reel = reelsService.getReelByIdWithCounts(reelId);
        return new ResponseEntity<>(reel, HttpStatus.OK);
    }
    
    @PutMapping("/like/{reelId}")
    public ResponseEntity<ReelsResponse> likeReel(@RequestHeader("Authorization") String jwt, @PathVariable UUID reelId) throws Exception {
        User reqUser = userService.getUserFromToken(jwt);
        Reels reel = reelsService.likeReel(reelId, reqUser.getId());
        ReelsResponse reelResponse = new ReelsResponse(reel);
        return new ResponseEntity<>(reelResponse, HttpStatus.OK);
    }
    
    // Comment endpoints
    @PostMapping("/{reelId}/comment")
    public ResponseEntity<CommentResponse> addCommentToReel(
            @RequestHeader("Authorization") String jwt, 
            @PathVariable UUID reelId, 
            @RequestBody CommentRequest commentRequest) throws Exception {
        
        // Validate parameters
        if (reelId == null) {
            throw new IllegalArgumentException("Reel ID cannot be null");
        }
        if (commentRequest == null || commentRequest.getContent() == null || commentRequest.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("Comment content cannot be empty");
        }
        
        User reqUser = userService.getUserFromToken(jwt);
        Comment comment = reelsService.addCommentToReel(reelId, commentRequest.getContent(), reqUser.getId());
        
        // Initialize lazy collections
        comment.getLikedBy().size();
        if (comment.getUser() != null) {
            comment.getUser().getFname();
        }
        
        CommentResponse commentResponse = new CommentResponse();
        commentResponse.setId(comment.getId());
        commentResponse.setContent(comment.getContent());
        commentResponse.setUser(comment.getUser());
        commentResponse.setCreatedAt(comment.getCreatedAt());
        commentResponse.setTotalLikes(comment.getLikedBy().size());
        
        // Check if current user liked this comment
        boolean isLiked = comment.getLikedBy().stream()
                .anyMatch(user -> user.getId().equals(reqUser.getId()));
        commentResponse.setIsLiked(isLiked);
        
        return new ResponseEntity<>(commentResponse, HttpStatus.OK);
    }
    
    @PutMapping("/{reelId}/comment/{commentId}")
    public ResponseEntity<CommentResponse> updateCommentOnReel(
            @RequestHeader("Authorization") String jwt, 
            @PathVariable UUID reelId, 
            @PathVariable UUID commentId,
            @RequestBody CommentRequest commentRequest) throws Exception {
        
        // Validate parameters
        if (reelId == null || commentId == null) {
            throw new IllegalArgumentException("Reel ID and Comment ID cannot be null");
        }
        if (commentRequest == null || commentRequest.getContent() == null || commentRequest.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("Comment content cannot be empty");
        }
        
        User reqUser = userService.getUserFromToken(jwt);
        Comment comment = reelsService.updateCommentOnReel(reelId, commentId, commentRequest.getContent(), reqUser.getId());
        
        // Initialize lazy collections
        comment.getLikedBy().size();
        if (comment.getUser() != null) {
            comment.getUser().getFname();
        }
        
        CommentResponse commentResponse = new CommentResponse();
        commentResponse.setId(comment.getId());
        commentResponse.setContent(comment.getContent());
        commentResponse.setUser(comment.getUser());
        commentResponse.setCreatedAt(comment.getCreatedAt());
        commentResponse.setTotalLikes(comment.getLikedBy().size());
        
        // Check if current user liked this comment
        boolean isLiked = comment.getLikedBy().stream()
                .anyMatch(user -> user.getId().equals(reqUser.getId()));
        commentResponse.setIsLiked(isLiked);
        
        return new ResponseEntity<>(commentResponse, HttpStatus.OK);
    }
    
    @DeleteMapping("/{reelId}/comment/{commentId}")
    public ResponseEntity<ReelsResponse> deleteCommentFromReel(
            @RequestHeader("Authorization") String jwt, 
            @PathVariable UUID reelId, 
            @PathVariable UUID commentId) throws Exception {
        
        // Validate parameters
        if (reelId == null || commentId == null) {
            throw new IllegalArgumentException("Reel ID and Comment ID cannot be null");
        }
        
        User reqUser = userService.getUserFromToken(jwt);
        Reels reel = reelsService.deleteCommentFromReel(reelId, commentId, reqUser.getId());
        ReelsResponse reelResponse = new ReelsResponse(reel);
        return new ResponseEntity<>(reelResponse, HttpStatus.OK);
    }
    
    // Paginated comments endpoint
    @GetMapping("/{reelId}/comments")
    public ResponseEntity<PaginatedResponse<CommentResponse>> getCommentsByReelIdPaginated(
            @PathVariable UUID reelId,
            @RequestHeader("Authorization") String jwt,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) throws Exception {
        
        // Validate parameters
        if (reelId == null) {
            throw new IllegalArgumentException("Reel ID cannot be null");
        }
        
        User reqUser = userService.getUserFromToken(jwt);
        PaginatedResponse<CommentResponse> comments = reelsService.getCommentsByReelIdPaginated(reelId, reqUser.getId(), page, size);
        return new ResponseEntity<>(comments, HttpStatus.OK);
    }
    
    // Save/Unsave endpoints
    @PutMapping("/save/{reelId}")
    public ResponseEntity<Reels> saveReelById(@RequestHeader("Authorization") String jwt, @PathVariable UUID reelId) throws Exception {
        User reqUser = userService.getUserFromToken(jwt);
        Reels reel = reelsService.savedReel(reelId, reqUser.getId());
        return new ResponseEntity<>(reel, HttpStatus.OK);
    }
    
    @PutMapping("/{reelId}/comments/like/{commentId}")
    public ResponseEntity<Comment> likeCommentOnReel(
            @RequestHeader("Authorization") String jwt, 
            @PathVariable UUID reelId,
            @PathVariable UUID commentId) throws Exception {
        User reqUser = userService.getUserFromToken(jwt);
        Comment comment = reelsService.likeCommentOnReel(reelId, commentId, reqUser.getId());
        return new ResponseEntity<>(comment, HttpStatus.OK);
    }
    
    @PutMapping("/comments/like/{commentId}")
    public ResponseEntity<Comment> likeCommentOnReelById(
            @RequestHeader("Authorization") String jwt, 
            @PathVariable UUID commentId) throws Exception {
        User reqUser = userService.getUserFromToken(jwt);
        Comment comment = reelsService.likeCommentOnReelById(commentId, reqUser.getId());
        return new ResponseEntity<>(comment, HttpStatus.OK);
    }
    
    @GetMapping("/saved/{userId}")
    public ResponseEntity<List<Reels>> getSavedReelsByUserId(@PathVariable UUID userId) throws Exception {
        List<Reels> savedReels = reelsService.findSavedReelsByUserId(userId);
        return new ResponseEntity<>(savedReels, HttpStatus.OK);
    }
    
    @GetMapping("/saved")
    public ResponseEntity<List<Reels>> getCurrentUserSavedReels(@RequestHeader("Authorization") String jwt) throws Exception {
        User reqUser = userService.getUserFromToken(jwt);
        List<Reels> savedReels = reelsService.findSavedReelsByUserId(reqUser.getId());
        return new ResponseEntity<>(savedReels, HttpStatus.OK);
    }
    
    @GetMapping("/saved/feed")
    public ResponseEntity<PaginatedResponse<Reels>> getCurrentUserSavedReelsFeed(
            @RequestHeader("Authorization") String jwt,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) throws Exception {
        User reqUser = userService.getUserFromToken(jwt);
        PaginatedResponse<Reels> savedReels = reelsService.findSavedReelsByUserIdPaginated(reqUser.getId(), page, size);
        return new ResponseEntity<>(savedReels, HttpStatus.OK);
    }
    
    @GetMapping("/saved/ids")
    public ResponseEntity<List<UUID>> getCurrentUserSavedReelIds(@RequestHeader("Authorization") String jwt) throws Exception {
        User reqUser = userService.getUserFromToken(jwt);
        List<UUID> savedReelIds = reelsService.findSavedReelIdsByUserId(reqUser.getId());
        return new ResponseEntity<>(savedReelIds, HttpStatus.OK);
    }
    
    @GetMapping("/saved/optimized/feed")
    public ResponseEntity<PaginatedResponse<ReelsResponse>> getCurrentUserSavedReelsFeedOptimized(
            @RequestHeader("Authorization") String jwt,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) throws Exception {
        User reqUser = userService.getUserFromToken(jwt);
        PaginatedResponse<ReelsResponse> savedReels = reelsService.findSavedReelsByUserIdOptimizedPaginated(reqUser.getId(), page, size);
        return new ResponseEntity<>(savedReels, HttpStatus.OK);
    }
}
