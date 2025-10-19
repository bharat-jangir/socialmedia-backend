package com.bharat.springbootsocial.services;

import com.bharat.springbootsocial.entity.Comment;

import java.util.List;
import java.util.UUID;

public interface CommentServices {
    Comment createComment(Comment comment, UUID postId, UUID userId) throws Exception;

    Comment findCommentById(UUID commentId) throws Exception;

    List<Comment> findCommentsByPostId(UUID postId) throws Exception;

    List<Comment> findCommentsByUserId(UUID userId) throws Exception;

    Comment updateComment(UUID commentId, String content, UUID userId) throws Exception;

    String deleteComment(UUID commentId, UUID userId) throws Exception;

    Comment likeComment(UUID commentId, UUID userId) throws Exception;
}
