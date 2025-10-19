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
@Table(name = "reels")
public class Reels {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String title;
    private String video;
    @ManyToOne
    @JsonIgnoreProperties({"savedPosts", "following", "followers"})
    private User user;
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "reels_liked_by", 
        joinColumns = @JoinColumn(name = "reels_id"), 
        inverseJoinColumns = @JoinColumn(name = "liked_by_id")
    )
    @JsonIgnoreProperties({"savedPosts", "following", "followers", "likedPosts"})
    private List<User> likedBy = new ArrayList<>();
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinTable(
        name = "reels_comments",
        joinColumns = @JoinColumn(name = "reels_id"),
        inverseJoinColumns = @JoinColumn(name = "comments_id")
    )
    @JsonIgnoreProperties({"user"})
    private List<Comment> comments = new ArrayList<>();
    private LocalDateTime createdAt;
}
