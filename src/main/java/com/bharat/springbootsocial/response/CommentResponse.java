package com.bharat.springbootsocial.response;

import com.bharat.springbootsocial.entity.User;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class CommentResponse {
    private UUID id;
    private String content;
    private User user;
    private Integer totalLikes;
    private Boolean isLiked;
    private LocalDateTime createdAt;
}