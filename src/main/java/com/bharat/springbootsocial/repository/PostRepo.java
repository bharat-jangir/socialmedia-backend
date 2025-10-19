package com.bharat.springbootsocial.repository;

import com.bharat.springbootsocial.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface PostRepo extends JpaRepository<Post,UUID> {
    @Query("SELECT DISTINCT p FROM Post p LEFT JOIN FETCH p.comments WHERE p.user.id = :userId")
    List<Post> findPostByUserId(UUID userId);
    
    @Query("SELECT DISTINCT p FROM Post p LEFT JOIN FETCH p.comments")
    List<Post> findAll();
    
    @Query("SELECT DISTINCT p FROM Post p LEFT JOIN FETCH p.comments WHERE p.id = :postId")
    Post findByIdWithComments(UUID postId);
    
    @Query("SELECT DISTINCT p FROM Post p LEFT JOIN FETCH p.likedBy WHERE p.id = :postId")
    Post findByIdWithLikes(UUID postId);
    
    // Paginated queries for infinite scroll
    @Query(value = "SELECT DISTINCT p FROM Post p LEFT JOIN FETCH p.comments ORDER BY p.createdAt DESC")
    Page<Post> findAllPostsPaginated(Pageable pageable);
    
    @Query(value = "SELECT DISTINCT p FROM Post p LEFT JOIN FETCH p.comments WHERE p.user.id = :userId ORDER BY p.createdAt DESC")
    Page<Post> findPostsByUserIdPaginated(UUID userId, Pageable pageable);
    
    // Note: For saved posts pagination, we'll handle it in the service layer
    // due to the complexity of the many-to-many relationship with pagination
    
    // Count posts by user ID
    @Query("SELECT COUNT(p) FROM Post p WHERE p.user.id = :userId")
    Long countPostsByUserId(UUID userId);
}
