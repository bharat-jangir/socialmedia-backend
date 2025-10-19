package com.bharat.springbootsocial.services;

import com.bharat.springbootsocial.entity.Comment;
import com.bharat.springbootsocial.entity.Reels;
import com.bharat.springbootsocial.entity.User;
import com.bharat.springbootsocial.response.PaginatedResponse;
import com.bharat.springbootsocial.response.ReelsResponse;
import com.bharat.springbootsocial.response.CommentResponse;

import java.util.List;
import java.util.UUID;

public interface ReelsService {
    Reels createReel(Reels reels, User user);
    List<Reels> findAllReels();
    List<Reels> findReelsByUserId(UUID userId);
    
    // Paginated methods for infinite scroll
    PaginatedResponse<Reels> findAllReelsPaginated(int page, int size);
    PaginatedResponse<Reels> findReelsByUserIdPaginated(UUID userId, int page, int size);
    
    // Like/Unlike methods
    Reels likeReel(UUID reelId, UUID userId) throws Exception;
    Reels findReelById(UUID reelId) throws Exception;
    
    // Delete method
    String deleteReel(UUID reelId, UUID userId) throws Exception;
    
    // Comment methods
    Comment addCommentToReel(UUID reelId, String content, UUID userId) throws Exception;
    Comment updateCommentOnReel(UUID reelId, UUID commentId, String content, UUID userId) throws Exception;
    Reels deleteCommentFromReel(UUID reelId, UUID commentId, UUID userId) throws Exception;
    Comment likeCommentOnReel(UUID reelId, UUID commentId, UUID userId) throws Exception;
    Comment likeCommentOnReelById(UUID commentId, UUID userId) throws Exception;
    
    // Paginated comment methods
    PaginatedResponse<CommentResponse> getCommentsByReelIdPaginated(UUID reelId, UUID currentUserId, int page, int size) throws Exception;
    
    // Save/Unsave methods
    Reels savedReel(UUID reelId, UUID userId) throws Exception;
    List<Reels> findSavedReelsByUserId(UUID userId) throws Exception;
    List<UUID> findSavedReelIdsByUserId(UUID userId) throws Exception;
    PaginatedResponse<Reels> findSavedReelsByUserIdPaginated(UUID userId, int page, int size) throws Exception;
    PaginatedResponse<ReelsResponse> findSavedReelsByUserIdOptimizedPaginated(UUID userId, int page, int size) throws Exception;
    
    // Response methods with counts
    ReelsResponse getReelByIdWithCounts(UUID reelId) throws Exception;
    List<ReelsResponse> getAllReelsWithCounts();
    List<ReelsResponse> getReelsByUserIdWithCounts(UUID userId);
    PaginatedResponse<ReelsResponse> getAllReelsPaginatedWithCounts(int page, int size);
    PaginatedResponse<ReelsResponse> getReelsByUserIdPaginatedWithCounts(UUID userId, int page, int size);
}
