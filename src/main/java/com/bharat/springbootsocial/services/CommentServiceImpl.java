package com.bharat.springbootsocial.services;

import com.bharat.springbootsocial.entity.Comment;
import com.bharat.springbootsocial.entity.Post;
import com.bharat.springbootsocial.entity.User;
import com.bharat.springbootsocial.repository.CommentRepo;
import com.bharat.springbootsocial.repository.PostRepo;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class CommentServiceImpl implements CommentServices {

    @Autowired
    private PostService postService;
    @Autowired
    private ServiceInt userService;
    
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private CommentRepo commentRepo;
    @Autowired
    private PostRepo postRepo;

    @Override
    public Comment createComment(Comment comment, UUID postId, UUID userId) throws Exception {
        User user = userService.getUserById(userId);
        Post post = postService.findPostById(postId);
        comment.setUser(user);

        comment.setContent(comment.getContent());
        comment.setCreatedAt(LocalDateTime.now());

        commentRepo.save(comment);
        post.getComments().add(comment);
        postRepo.save(post);
        
        // Send comment notification
        notificationService.sendCommentNotification(post.getUser(), user, "POST", post.getId());

        return comment;
    }

    @Override
    public Comment likeComment(UUID commentId, UUID userId) throws Exception {
        Comment comment = findCommentById(commentId);
        User user = userService.getUserById(userId);
        
        // Check if user has already liked the comment using ID comparison
        boolean isLiked = comment.getLikedBy().stream()
                .anyMatch(likedUser -> likedUser.getId().equals(userId));
        
        if (isLiked) {
            // Remove like
            comment.getLikedBy().removeIf(likedUser -> likedUser.getId().equals(userId));
        } else {
            // Add like
            comment.getLikedBy().add(user);
            
            // Send like notification
            notificationService.sendLikeNotification(comment.getUser(), user, "COMMENT", comment.getId());
        }
        
        return commentRepo.save(comment);
    }

    @Override
    public Comment findCommentById(UUID commentId) throws Exception {
        Optional<Comment> commentOptional = commentRepo.findById(commentId);
        if (commentOptional.isEmpty()) {
            throw new Exception("No comment found with id: " + commentId);
        }
        Comment comment = commentOptional.get();
        // Initialize lazy collection to load likes
        comment.getLikedBy().size();
        return comment;
    }

    @Override
    public List<Comment> findCommentsByPostId(UUID postId) throws Exception {
        Post post = postService.findPostById(postId);
        List<Comment> comments = post.getComments();
        // Initialize lazy collections for each comment
        for (Comment comment : comments) {
            comment.getLikedBy().size(); // Trigger lazy loading
        }
        return comments;
    }

    @Override
    public List<Comment> findCommentsByUserId(UUID userId) throws Exception {
        List<Comment> comments = commentRepo.findByUserId(userId);
        // Initialize lazy collections for each comment
        for (Comment comment : comments) {
            comment.getLikedBy().size(); // Trigger lazy loading
        }
        return comments;
    }

    @Override
    public Comment updateComment(UUID commentId, String content, UUID userId) throws Exception {
        Comment comment = findCommentById(commentId);
        if (!comment.getUser().getId().equals(userId)) {
            throw new Exception("You are not authorized to update this comment");
        }
        comment.setContent(content);
        return commentRepo.save(comment);
    }

    @Override
    public String deleteComment(UUID commentId, UUID userId) throws Exception {
        Comment comment = findCommentById(commentId);
        if (!comment.getUser().getId().equals(userId)) {
            throw new Exception("You are not authorized to delete this comment");
        }

        // Find the post that contains this comment and remove the comment from it
        Post postWithComment = findPostByCommentId(commentId);
        if (postWithComment != null) {
            postWithComment.getComments().removeIf(c -> c.getId().equals(commentId));
            postRepo.save(postWithComment);
        } else {
            // If not found in any post, delete directly
            commentRepo.deleteById(commentId);
        }

        return "Comment deleted successfully";
    }

    private Post findPostByCommentId(UUID commentId) {
        List<Post> allPosts = postRepo.findAll();
        for (Post post : allPosts) {
            if (post.getComments().stream().anyMatch(c -> c.getId().equals(commentId))) {
                return post;
            }
        }
        return null;
    }
}
