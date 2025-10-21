package com.bharat.springbootsocial.controller;

import com.bharat.springbootsocial.entity.Post;
import com.bharat.springbootsocial.entity.User;
import java.util.UUID;
import com.bharat.springbootsocial.response.ApiResponse;
import com.bharat.springbootsocial.response.PaginatedResponse;
import com.bharat.springbootsocial.response.PostResponse;
import com.bharat.springbootsocial.response.CommentResponse;
import com.bharat.springbootsocial.response.ContentResponse;
import com.bharat.springbootsocial.services.PostService;
import com.bharat.springbootsocial.services.ServiceInt;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/posts")
@AllArgsConstructor
public class PostController {

    @Autowired
    private PostService postService;
    @Autowired
    private ServiceInt userServices;

    @PostMapping()
    public ResponseEntity<Post> createPost(@RequestHeader("Authorization") String jwt, @RequestBody Post post)
            throws Exception {
        User reqUser = userServices.getUserFromToken(jwt);
        Post newPost = postService.createPost(post, reqUser.getId());
        return new ResponseEntity<>(newPost, HttpStatus.CREATED);
    }

    @DeleteMapping("{postId}")
    public ResponseEntity<ApiResponse> deletePost(@RequestHeader("Authorization") String jwt, @PathVariable UUID postId)
            throws Exception {
        User reqUser = userServices.getUserFromToken(jwt);
        String message = postService.deletePost(postId, reqUser.getId());
        ApiResponse response = new ApiResponse(message, true);
        return new ResponseEntity<ApiResponse>(response, HttpStatus.OK);
    }

    @GetMapping("{postId}")
    public ResponseEntity<Post> findPostById(@PathVariable UUID postId) throws Exception {
        Post post = postService.findPostById(postId);
        return new ResponseEntity<>(post, HttpStatus.OK);
    }

    @GetMapping("user/{userId}")
    public ResponseEntity<List<ContentResponse>> findPostsByUserId(
            @PathVariable UUID userId,
            @RequestHeader(value = "Authorization", required = false) String jwt) throws Exception {
        UUID currentUserId = null;
        if (jwt != null && !jwt.isEmpty()) {
            try {
                User reqUser = userServices.getUserFromToken(jwt);
                currentUserId = reqUser.getId();
            } catch (Exception e) {
                // If token is invalid, continue without current user context
            }
        }
        
        List<ContentResponse> postsAndReels = postService.findPostsAndReelsByUserIdWithType(userId, currentUserId);
        return new ResponseEntity<>(postsAndReels, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<Post>> findAllPosts() throws Exception {
        List<Post> posts = postService.findAllPosts();
        return new ResponseEntity<>(posts, HttpStatus.OK);
    }

    @PutMapping("save/{postId}")
    public ResponseEntity<Post> savePostById(@RequestHeader("Authorization") String jwt, @PathVariable UUID postId)
            throws Exception {
        User reqUser = userServices.getUserFromToken(jwt);
        Post post = postService.savedPost(postId, reqUser.getId());
        return new ResponseEntity<>(post, HttpStatus.OK);
    }

    @PutMapping("like/{postId}")
    public ResponseEntity<Post> likePost(@RequestHeader("Authorization") String jwt, @PathVariable UUID postId)
            throws Exception {
        User reqUser = userServices.getUserFromToken(jwt);
        Post post = postService.likedPost(postId, reqUser.getId());
        return new ResponseEntity<>(post, HttpStatus.OK);
    }

    @GetMapping("saved/{userId}")
    public ResponseEntity<List<Post>> getSavedPostsByUserId(@PathVariable UUID userId) throws Exception {
        List<Post> savedPosts = postService.findSavedPostsByUserId(userId);
        return new ResponseEntity<>(savedPosts, HttpStatus.OK);
    }

    @GetMapping("saved")
    public ResponseEntity<List<Post>> getCurrentUserSavedPosts(@RequestHeader("Authorization") String jwt)
            throws Exception {
        User reqUser = userServices.getUserFromToken(jwt);
        List<Post> savedPosts = postService.findSavedPostsByUserId(reqUser.getId());
        return new ResponseEntity<>(savedPosts, HttpStatus.OK);
    }
    
    // Combined saved content endpoints (posts + reels)
    @GetMapping("saved/combined/{userId}")
    public ResponseEntity<List<ContentResponse>> getCombinedSavedContentByUserId(@PathVariable UUID userId) throws Exception {
        List<ContentResponse> combinedContent = postService.findCombinedSavedContentByUserId(userId);
        return new ResponseEntity<>(combinedContent, HttpStatus.OK);
    }
    
    @GetMapping("saved/combined")
    public ResponseEntity<List<ContentResponse>> getCurrentUserCombinedSavedContent(@RequestHeader("Authorization") String jwt)
            throws Exception {
        User reqUser = userServices.getUserFromToken(jwt);
        List<ContentResponse> combinedContent = postService.findCombinedSavedContentByUserId(reqUser.getId());
        return new ResponseEntity<>(combinedContent, HttpStatus.OK);
    }
    
    @GetMapping("saved/combined/paginated")
    public ResponseEntity<PaginatedResponse<ContentResponse>> getCurrentUserCombinedSavedContentPaginated(
            @RequestHeader("Authorization") String jwt,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) throws Exception {
        User reqUser = userServices.getUserFromToken(jwt);
        PaginatedResponse<ContentResponse> combinedContent = postService.findCombinedSavedContentByUserIdPaginated(reqUser.getId(), page, size);
        return new ResponseEntity<>(combinedContent, HttpStatus.OK);
    }
    
    // Infinite scroll endpoints
    @GetMapping("/feed")
    public ResponseEntity<PaginatedResponse<Post>> getPostsFeed(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) throws Exception {
        PaginatedResponse<Post> posts = postService.findAllPostsPaginated(page, size);
        return new ResponseEntity<>(posts, HttpStatus.OK);
    }
    
    @GetMapping("/user/{userId}/feed")
    public ResponseEntity<PaginatedResponse<Post>> getUserPostsFeed(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) throws Exception {
        PaginatedResponse<Post> posts = postService.findPostsByUserIdPaginated(userId, page, size);
        return new ResponseEntity<>(posts, HttpStatus.OK);
    }
    
    @GetMapping("/saved/feed")
    public ResponseEntity<PaginatedResponse<Post>> getCurrentUserSavedPostsFeed(
            @RequestHeader("Authorization") String jwt,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) throws Exception {
        User reqUser = userServices.getUserFromToken(jwt);
        PaginatedResponse<Post> savedPosts = postService.findSavedPostsByUserIdPaginated(reqUser.getId(), page, size);
        return new ResponseEntity<>(savedPosts, HttpStatus.OK);
    }
    
    @GetMapping("/saved/ids")
    public ResponseEntity<List<UUID>> getCurrentUserSavedPostIds(@RequestHeader("Authorization") String jwt) throws Exception {
        User reqUser = userServices.getUserFromToken(jwt);
        List<UUID> savedPostIds = postService.findSavedPostIdsByUserId(reqUser.getId());
        return new ResponseEntity<>(savedPostIds, HttpStatus.OK);
    }
    
    // Optimized endpoints for better UI performance
    @GetMapping("/optimized")
    public ResponseEntity<List<PostResponse>> findAllPostsOptimized(
            @RequestHeader("Authorization") String jwt) throws Exception {
        User reqUser = userServices.getUserFromToken(jwt);
        List<PostResponse> posts = postService.findAllPostsOptimized(reqUser.getId());
        return new ResponseEntity<>(posts, HttpStatus.OK);
    }
    
    @GetMapping("/optimized/feed")
    public ResponseEntity<PaginatedResponse<PostResponse>> getPostsFeedOptimized(
            @RequestHeader("Authorization") String jwt,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) throws Exception {
        User reqUser = userServices.getUserFromToken(jwt);
        PaginatedResponse<PostResponse> posts = postService.findAllPostsOptimizedPaginated(reqUser.getId(), page, size);
        return new ResponseEntity<>(posts, HttpStatus.OK);
    }
    
    @GetMapping("/user/{userId}/optimized")
    public ResponseEntity<List<PostResponse>> findPostsByUserIdOptimized(
            @PathVariable UUID userId,
            @RequestHeader("Authorization") String jwt) throws Exception {
        User reqUser = userServices.getUserFromToken(jwt);
        List<PostResponse> posts = postService.findPostsByUserIdOptimized(userId, reqUser.getId());
        return new ResponseEntity<>(posts, HttpStatus.OK);
    }
    
    @GetMapping("/user/{userId}/optimized/feed")
    public ResponseEntity<PaginatedResponse<PostResponse>> getUserPostsFeedOptimized(
            @PathVariable UUID userId,
            @RequestHeader("Authorization") String jwt,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) throws Exception {
        User reqUser = userServices.getUserFromToken(jwt);
        PaginatedResponse<PostResponse> posts = postService.findPostsByUserIdOptimizedPaginated(userId, reqUser.getId(), page, size);
        return new ResponseEntity<>(posts, HttpStatus.OK);
    }
    
    @GetMapping("/optimized/saved/feed")
    public ResponseEntity<PaginatedResponse<PostResponse>> getCurrentUserSavedPostsFeedOptimized(
            @RequestHeader("Authorization") String jwt,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) throws Exception {
        User reqUser = userServices.getUserFromToken(jwt);
        PaginatedResponse<PostResponse> savedPosts = postService.findSavedPostsByUserIdOptimizedPaginated(reqUser.getId(), page, size);
        return new ResponseEntity<>(savedPosts, HttpStatus.OK);
    }
    
    // Get post likes with pagination
    @GetMapping("/{postId}/likes")
    public ResponseEntity<PaginatedResponse<User>> getPostLikes(
            @PathVariable UUID postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) throws Exception {
        PaginatedResponse<User> likes = postService.getPostLikesPaginated(postId, page, size);
        return new ResponseEntity<>(likes, HttpStatus.OK);
    }
    
    // Get post comments with pagination
    @GetMapping("/{postId}/comments")
    public ResponseEntity<PaginatedResponse<CommentResponse>> getPostComments(
            @PathVariable UUID postId,
            @RequestHeader("Authorization") String jwt,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) throws Exception {
        User reqUser = userServices.getUserFromToken(jwt);
        PaginatedResponse<CommentResponse> comments = postService.getPostCommentsPaginated(postId, reqUser.getId(), page, size);
        return new ResponseEntity<>(comments, HttpStatus.OK);
    }
}
