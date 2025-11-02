package com.bharat.springbootsocial.repository;

import com.bharat.springbootsocial.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface CommentRepo extends JpaRepository<Comment, UUID> {
    List<Comment> findByUserId(UUID userId);
    
    // For Posts
    List<Comment> findAllByPostId(UUID postId);
    
    // For Reels - use JPQL to leverage Hibernate's UUID handling
    @Query("SELECT c FROM Comment c WHERE c.reel.id = :reelId ORDER BY c.createdAt DESC")
    Page<Comment> findCommentsByReelIdPaginated(@Param("reelId") UUID reelId, Pageable pageable);
    
    // For Reels (non-paginated)
    @Query("SELECT c FROM Comment c WHERE c.reel.id = :reelId ORDER BY c.createdAt DESC")
    List<Comment> findAllByReelId(@Param("reelId") UUID reelId);
}
