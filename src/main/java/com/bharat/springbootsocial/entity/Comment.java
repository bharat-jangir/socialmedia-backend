package com.bharat.springbootsocial.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "comments")
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;
    private String content;

    @ManyToOne
    @JsonIgnoreProperties({"savedPosts", "following", "followers"})
    private User user;

    @ManyToOne
    @JsonIgnoreProperties({"comments", "likedBy"})
    private Post post;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "comments_liked_by", 
        joinColumns = @JoinColumn(name = "comments_id", columnDefinition = "BINARY(16)"), 
        inverseJoinColumns = @JoinColumn(name = "liked_by_id", columnDefinition = "BINARY(16)")
    )
    @JsonIgnoreProperties({"savedPosts", "following", "followers", "likedPosts"})
    private List<User> likedBy = new ArrayList<>();

    private LocalDateTime createdAt;
}
