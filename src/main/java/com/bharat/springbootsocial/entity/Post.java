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
@Table(name = "posts")
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;
    private String caption;
    private String image;
    private String video;
    @ManyToOne
    @JsonIgnoreProperties({"savedPosts", "following", "followers"})
    private User user;
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "posts_liked_by", 
        joinColumns = @JoinColumn(name = "posts_id"), 
        inverseJoinColumns = @JoinColumn(name = "liked_by_id")
    )
    @JsonIgnoreProperties({"savedPosts", "following", "followers", "likedPosts"})
    private List<User> likedBy = new ArrayList<>();
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JsonIgnoreProperties({"user"})
    private List<Comment> comments = new ArrayList<>();
    private LocalDateTime createdAt;
}
