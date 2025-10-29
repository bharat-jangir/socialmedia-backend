package com.bharat.springbootsocial.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "users")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;
    @Column(name = "first_name")
    private String fname;
    @Column(name = "last_name")
    private String lname;
    private String email;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;
    private String gender;
    @Column(name = "profile_image")
    private String profileImage;
    @Column(name = "cover_image")
    private String coverImage;
    @Column(name = "user_bio", length = 500)
    private String userBio;

    private List<String> following = new ArrayList<>();
    private List<String> followers = new ArrayList<>();
    @ManyToMany // Many users can save many posts
    @JoinTable(
        name = "users_saved_posts",
        joinColumns = @JoinColumn(name = "user_id", columnDefinition = "BINARY(16)"),
        inverseJoinColumns = @JoinColumn(name = "post_id", columnDefinition = "BINARY(16)")
    )
    @JsonIgnore
    private List<Post> savedPosts = new ArrayList<>();
    
    @ManyToMany // Many users can save many reels
    @JoinTable(
        name = "users_saved_reels",
        joinColumns = @JoinColumn(name = "user_id", columnDefinition = "BINARY(16)"),
        inverseJoinColumns = @JoinColumn(name = "reel_id", columnDefinition = "BINARY(16)")
    )
    @JsonIgnore
    private List<Reels> savedReels = new ArrayList<>();
    
    @ManyToMany(mappedBy = "likedBy") // Many users can like many posts
    @JsonIgnore
    private List<Post> likedPosts = new ArrayList<>();
    
    @ManyToMany(mappedBy = "likedBy") // Many users can like many comments
    @JsonIgnore
    private List<Comment> likedComments = new ArrayList<>();

}
