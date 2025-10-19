package com.bharat.springbootsocial.repository;

import com.bharat.springbootsocial.entity.Reels;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface ReelsRepo extends JpaRepository<Reels, UUID> {
    List<Reels> findByUserId(UUID userId);
    
    // Paginated queries for infinite scroll
    @Query(value = "SELECT r FROM Reels r ORDER BY r.id DESC")
    Page<Reels> findAllReelsPaginated(Pageable pageable);
    
    @Query(value = "SELECT r FROM Reels r WHERE r.user.id = :userId ORDER BY r.id DESC")
    Page<Reels> findReelsByUserIdPaginated(UUID userId, Pageable pageable);
    
    // Like-related queries
    @Query("SELECT DISTINCT r FROM Reels r LEFT JOIN FETCH r.likedBy WHERE r.id = :reelId")
    Reels findByIdWithLikes(UUID reelId);
    
    // Comment-related queries
    @Query("SELECT DISTINCT r FROM Reels r LEFT JOIN FETCH r.comments WHERE r.id = :reelId")
    Reels findByIdWithComments(UUID reelId);
    
    @Query("SELECT DISTINCT r FROM Reels r LEFT JOIN FETCH r.comments")
    List<Reels> findAllWithComments();
    
    @Query("SELECT DISTINCT r FROM Reels r LEFT JOIN FETCH r.comments WHERE r.user.id = :userId")
    List<Reels> findByUserIdWithComments(UUID userId);
    
    // Count reels by user ID
    @Query("SELECT COUNT(r) FROM Reels r WHERE r.user.id = :userId")
    Long countReelsByUserId(UUID userId);
    
}
