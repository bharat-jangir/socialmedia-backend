package com.bharat.springbootsocial.repository;

import com.bharat.springbootsocial.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface CommentRepo extends JpaRepository<Comment, UUID> {
    List<Comment> findByUserId(UUID userId);
    
    // Paginated comment queries for reels
    @Query(value = "SELECT c.* FROM comments c INNER JOIN reels_comments rc ON c.id = rc.comments_id WHERE rc.reels_id = :reelId ORDER BY c.created_at DESC", nativeQuery = true)
    Page<Comment> findCommentsByReelIdPaginated(UUID reelId, Pageable pageable);
}
