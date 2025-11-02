package com.bharat.springbootsocial.services;

import com.bharat.springbootsocial.entity.Comment;
import com.bharat.springbootsocial.entity.Post;
import com.bharat.springbootsocial.entity.Reels;
import com.bharat.springbootsocial.entity.User;
import com.bharat.springbootsocial.repository.CommentRepo;
import com.bharat.springbootsocial.repository.PostRepo;
import com.bharat.springbootsocial.repository.ReelsRepo;
import com.bharat.springbootsocial.repository.UserRepo;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentServices {

    private final CommentRepo commentRepo;
    private final UserRepo userRepo;
    private final PostRepo postRepo;
    private final ReelsRepo reelsRepo;

    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Autowired
    private NotificationService notificationService;

    @Override
    @Transactional
    public Comment createComment(Comment comment, UUID postId, UUID userId, UUID reelId) throws Exception {
        // Load user entity
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new Exception("User not found with id: " + userId));

        // Set basic fields
        comment.setUser(user);
        // Use UTC to ensure consistent timezone handling
        comment.setCreatedAt(LocalDateTime.now(ZoneOffset.UTC));

        // Validate only one parent
        if (postId != null && reelId != null) {
            throw new Exception("Comment cannot be associated with both post and reel");
        }

        // Set post relationship
        if (postId != null) {
            Post post = postRepo.findById(postId)
                    .orElseThrow(() -> new Exception("Post not found with id: " + postId));
            comment.setPost(post);
            comment.setReel(null);
    }

        // Set reel relationship
        if (reelId != null) {
            Reels reel = reelsRepo.findById(reelId)
                    .orElseThrow(() -> new Exception("Reel not found with id: " + reelId));
            comment.setReel(reel);
            comment.setPost(null);
        }
        
        // Save using Hibernate/JPA
        Comment savedComment = commentRepo.save(comment);
        
        // WORKAROUND: Hibernate 6.x bug - directly update foreign key after save
        // This guarantees the foreign key is set correctly
        if (postId != null) {
            String commentIdHex = savedComment.getId().toString().replace("-", "");
            String postIdHex = postId.toString().replace("-", "");
            jdbcTemplate.update(
                "UPDATE comments SET post_id = UNHEX(?) WHERE id = UNHEX(?)",
                postIdHex, commentIdHex
            );
            // Reload to get updated relationship
            savedComment = commentRepo.findById(savedComment.getId()).orElse(savedComment);
        }
        
        if (reelId != null) {
            String commentIdHex = savedComment.getId().toString().replace("-", "");
            String reelIdHex = reelId.toString().replace("-", "");
            jdbcTemplate.update(
                "UPDATE comments SET reel_id = UNHEX(?) WHERE id = UNHEX(?)",
                reelIdHex, commentIdHex
            );
            // Reload to get updated relationship
            savedComment = commentRepo.findById(savedComment.getId()).orElse(savedComment);
        }
        
        // Send notification if comment is for a post
        if (postId != null && savedComment.getPost() != null) {
            notificationService.sendCommentNotification(savedComment.getPost().getUser(), user, "POST", savedComment.getPost().getId());
        }
        
        return savedComment;
    }
    

    @Override
    public Comment findCommentById(UUID commentId) throws Exception {
        return commentRepo.findById(commentId)
                .orElseThrow(() -> new Exception("Comment not found with id: " + commentId));
    }

    @Override
    public List<Comment> findCommentsByPostId(UUID postId) throws Exception {
        return commentRepo.findAllByPostId(postId);
    }

    @Override
    public List<Comment> findCommentsByUserId(UUID userId) throws Exception {
        return commentRepo.findByUserId(userId);
    }

    @Override
    @Transactional
    public Comment updateComment(UUID commentId, String content, UUID userId) throws Exception {
        Comment comment = findCommentById(commentId);
        if (!comment.getUser().getId().equals(userId)) {
            throw new Exception("You can only edit your own comments");
        }
        comment.setContent(content);
        return commentRepo.save(comment);
    }

    @Override
    @Transactional
    public String deleteComment(UUID commentId, UUID userId) throws Exception {
        Comment comment = findCommentById(commentId);
        if (!comment.getUser().getId().equals(userId)) {
            throw new Exception("You can only delete your own comments");
        }
        commentRepo.delete(comment);
        return "Comment deleted successfully";
    }

    @Override
    @Transactional
    public Comment likeComment(UUID commentId, UUID userId) throws Exception {
        Comment comment = findCommentById(commentId);
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new Exception("User not found"));

        if (comment.getLikedBy().contains(user)) {
            comment.getLikedBy().remove(user);
        } else {
            comment.getLikedBy().add(user);
            // Send like notification
            notificationService.sendLikeNotification(comment.getUser(), user, "COMMENT", comment.getId());
            }

        return commentRepo.save(comment);
    }
}