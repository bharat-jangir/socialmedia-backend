package com.bharat.springbootsocial.repository;

import com.bharat.springbootsocial.entity.Story;
import com.bharat.springbootsocial.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface StoryRepo extends JpaRepository<Story, UUID> {
    
    // Find active stories by user ID
    @Query("SELECT s FROM Story s WHERE s.user.id = :userId AND s.isActive = true AND s.expiresAt > :now ORDER BY s.createdAt DESC")
    List<Story> findActiveStoriesByUserId(@Param("userId") UUID userId, @Param("now") LocalDateTime now);
    
    // Find all stories by user ID (including expired)
    List<Story> findByUserIdOrderByCreatedAtDesc(UUID userId);
    
    // Find stories from users that the current user follows
    @Query("SELECT s FROM Story s WHERE s.user.id IN :followingIds AND s.isActive = true AND s.expiresAt > :now ORDER BY s.createdAt DESC")
    List<Story> findActiveStoriesFromFollowing(@Param("followingIds") List<String> followingIds, @Param("now") LocalDateTime now);
    
    // Find paginated stories from users that the current user follows
    @Query("SELECT s FROM Story s WHERE s.user.id IN :followingIds AND s.isActive = true AND s.expiresAt > :now ORDER BY s.createdAt DESC")
    Page<Story> findActiveStoriesFromFollowingPaginated(@Param("followingIds") List<String> followingIds, @Param("now") LocalDateTime now, Pageable pageable);
    
    // Find expired stories for cleanup
    @Query("SELECT s FROM Story s WHERE s.expiresAt <= :now AND s.isActive = true")
    List<Story> findExpiredStories(@Param("now") LocalDateTime now);
    
    // Count active stories by user
    @Query("SELECT COUNT(s) FROM Story s WHERE s.user.id = :userId AND s.isActive = true AND s.expiresAt > :now")
    Integer countActiveStoriesByUserId(@Param("userId") UUID userId, @Param("now") LocalDateTime now);
    
    // Check if user has unviewed stories
    @Query("SELECT COUNT(s) FROM Story s WHERE s.user.id = :userId AND s.isActive = true AND s.expiresAt > :now AND :viewer NOT MEMBER OF s.viewedBy")
    Integer countUnviewedStoriesByUserId(@Param("userId") UUID userId, @Param("viewer") User viewer, @Param("now") LocalDateTime now);
    
    // Find stories that need to be deactivated
    @Query("SELECT s FROM Story s WHERE s.expiresAt <= :now")
    List<Story> findStoriesToDeactivate(@Param("now") LocalDateTime now);
}
