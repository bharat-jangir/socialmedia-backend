package com.bharat.springbootsocial.controller;

import com.bharat.springbootsocial.entity.Comment;
import com.bharat.springbootsocial.entity.User;
import java.util.UUID;
import com.bharat.springbootsocial.response.ApiResponse;
import com.bharat.springbootsocial.response.CommentResponse;
import com.bharat.springbootsocial.services.CommentServices;
import com.bharat.springbootsocial.services.ServiceInt;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping("/api/comments")
@CrossOrigin(origins = "http://localhost:5173")
public class CommentController {
    @Autowired
    private CommentServices commentServices;
    @Autowired
    private ServiceInt userService;

    @PostMapping("post/{postId}")
    public ResponseEntity<CommentResponse> createComment(@RequestBody Comment comment,
            @RequestHeader("Authorization") String jwt, @PathVariable("postId") UUID postId) throws Exception {
        User user = userService.getUserFromToken(jwt);
        Comment createdComment = commentServices.createComment(comment, postId, user.getId());
        
        // Convert to CommentResponse with totalLikes and isLiked
        CommentResponse commentResponse = convertToCommentResponse(createdComment, user.getId());
        return new ResponseEntity<>(commentResponse, HttpStatus.CREATED);
    }

    @GetMapping("{commentId}")
    public ResponseEntity<CommentResponse> getCommentById(@PathVariable("commentId") UUID commentId, 
            @RequestHeader("Authorization") String jwt) throws Exception {
        User user = userService.getUserFromToken(jwt);
        Comment comment = commentServices.findCommentById(commentId);
        
        // Convert to CommentResponse with totalLikes and isLiked
        CommentResponse commentResponse = convertToCommentResponse(comment, user.getId());
        return new ResponseEntity<>(commentResponse, HttpStatus.OK);
    }

    @GetMapping("post/{postId}")
    public ResponseEntity<List<CommentResponse>> getCommentsByPost(@PathVariable("postId") UUID postId,
            @RequestHeader("Authorization") String jwt) throws Exception {
        User user = userService.getUserFromToken(jwt);
        List<Comment> comments = commentServices.findCommentsByPostId(postId);
        
        // Convert to CommentResponse with totalLikes and isLiked
        List<CommentResponse> commentResponses = comments.stream()
                .map(comment -> convertToCommentResponse(comment, user.getId()))
                .collect(java.util.stream.Collectors.toList());
        
        return new ResponseEntity<>(commentResponses, HttpStatus.OK);
    }

    @PutMapping("{commentId}")
    public ResponseEntity<CommentResponse> updateComment(@PathVariable("commentId") UUID commentId,
            @RequestBody Comment comment, @RequestHeader("Authorization") String jwt) throws Exception {
        User user = userService.getUserFromToken(jwt);
        Comment updatedComment = commentServices.updateComment(commentId, comment.getContent(), user.getId());
        
        // Convert to CommentResponse with totalLikes and isLiked
        CommentResponse commentResponse = convertToCommentResponse(updatedComment, user.getId());
        return new ResponseEntity<>(commentResponse, HttpStatus.OK);
    }

    @DeleteMapping("{commentId}")
    public ResponseEntity<ApiResponse> deleteComment(@PathVariable("commentId") UUID commentId,
            @RequestHeader("Authorization") String jwt) throws Exception {
        User user = userService.getUserFromToken(jwt);
        String message = commentServices.deleteComment(commentId, user.getId());
        ApiResponse response = new ApiResponse(message, true);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("like/{commentId}")
    public ResponseEntity<CommentResponse> likeComment(@RequestHeader("Authorization") String jwt,
            @PathVariable("commentId") UUID commentId) throws Exception {
        User user = userService.getUserFromToken(jwt);
        Comment comment = commentServices.likeComment(commentId, user.getId());
        
        // Convert to CommentResponse with totalLikes and isLiked
        CommentResponse commentResponse = convertToCommentResponse(comment, user.getId());
        return new ResponseEntity<>(commentResponse, HttpStatus.OK);
    }

    @GetMapping("user/{userId}")
    public ResponseEntity<List<CommentResponse>> getCommentsByUser(@PathVariable("userId") UUID userId,
            @RequestHeader("Authorization") String jwt) throws Exception {
        User user = userService.getUserFromToken(jwt);
        List<Comment> comments = commentServices.findCommentsByUserId(userId);
        
        // Convert to CommentResponse with totalLikes and isLiked
        List<CommentResponse> commentResponses = comments.stream()
                .map(comment -> convertToCommentResponse(comment, user.getId()))
                .collect(java.util.stream.Collectors.toList());
        
        return new ResponseEntity<>(commentResponses, HttpStatus.OK);
    }
    
    private CommentResponse convertToCommentResponse(Comment comment, UUID currentUserId) {
        // Initialize lazy collections
        comment.getLikedBy().size(); // Trigger lazy loading
        if (comment.getUser() != null) {
            comment.getUser().getFname(); // Trigger lazy loading
        }
        
        CommentResponse response = new CommentResponse();
        response.setId(comment.getId());
        response.setContent(comment.getContent());
        response.setUser(comment.getUser());
        response.setCreatedAt(comment.getCreatedAt());
        
        // Set total likes count
        response.setTotalLikes(comment.getLikedBy().size());
        
        // Check if current user liked this comment
        boolean isLiked = currentUserId != null && comment.getLikedBy().stream()
                .anyMatch(user -> user.getId().equals(currentUserId));
        response.setIsLiked(isLiked);
        
        return response;
    }
}
