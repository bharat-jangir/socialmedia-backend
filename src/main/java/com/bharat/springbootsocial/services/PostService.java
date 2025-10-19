package com.bharat.springbootsocial.services;

import com.bharat.springbootsocial.entity.Post;
import com.bharat.springbootsocial.entity.User;
import com.bharat.springbootsocial.response.PaginatedResponse;
import com.bharat.springbootsocial.response.PostResponse;
import com.bharat.springbootsocial.response.CommentResponse;
import com.bharat.springbootsocial.response.ContentResponse;

import java.util.List;
import java.util.UUID;

public interface PostService {
    Post createPost(Post post, UUID userId) throws Exception;

    String deletePost(UUID postId, UUID userId) throws Exception;

    List<Post> findPostsByUserId(UUID userId) throws Exception;

    Post findPostById(UUID postId) throws Exception;

    List<Post> findAllPosts() throws Exception;

    Post savedPost(UUID postId, UUID userId) throws Exception;

    Post likedPost(UUID postId, UUID userId) throws Exception;

    List<Post> findSavedPostsByUserId(UUID userId) throws Exception;
    
    // Paginated methods for infinite scroll
    PaginatedResponse<Post> findAllPostsPaginated(int page, int size) throws Exception;
    
    PaginatedResponse<Post> findPostsByUserIdPaginated(UUID userId, int page, int size) throws Exception;
    
    PaginatedResponse<Post> findSavedPostsByUserIdPaginated(UUID userId, int page, int size) throws Exception;
    
    // Get saved post IDs only
    List<UUID> findSavedPostIdsByUserId(UUID userId) throws Exception;
    
    // Combined saved content (posts + reels) with type field
    List<ContentResponse> findCombinedSavedContentByUserId(UUID userId) throws Exception;
    PaginatedResponse<ContentResponse> findCombinedSavedContentByUserIdPaginated(UUID userId, int page, int size) throws Exception;
    
    // Optimized methods for better UI performance
    List<PostResponse> findAllPostsOptimized(UUID currentUserId) throws Exception;
    
    PaginatedResponse<PostResponse> findAllPostsOptimizedPaginated(UUID currentUserId, int page, int size) throws Exception;
    
    List<PostResponse> findPostsByUserIdOptimized(UUID userId, UUID currentUserId) throws Exception;
    
    PaginatedResponse<PostResponse> findPostsByUserIdOptimizedPaginated(UUID userId, UUID currentUserId, int page, int size) throws Exception;
    
    PaginatedResponse<PostResponse> findSavedPostsByUserIdOptimizedPaginated(UUID userId, int page, int size) throws Exception;
    
    // Get post likes and comments with pagination
    PaginatedResponse<User> getPostLikesPaginated(UUID postId, int page, int size) throws Exception;
    
    PaginatedResponse<CommentResponse> getPostCommentsPaginated(UUID postId, UUID currentUserId, int page, int size) throws Exception;
    
    // Get combined posts and reels for a user
    List<Object> findPostsAndReelsByUserId(UUID userId) throws Exception;
    
    // Get optimized combined posts and reels feed
    PaginatedResponse<ContentResponse> findAllPostsAndReelsOptimizedPaginated(UUID currentUserId, int page, int size) throws Exception;
    
    // Get combined posts and reels for a specific user with type information
    List<ContentResponse> findPostsAndReelsByUserIdWithType(UUID userId, UUID currentUserId) throws Exception;
}
