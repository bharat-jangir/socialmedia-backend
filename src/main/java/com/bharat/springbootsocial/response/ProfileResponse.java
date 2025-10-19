package com.bharat.springbootsocial.response;

import com.bharat.springbootsocial.entity.Post;
import com.bharat.springbootsocial.entity.Reels;
import com.bharat.springbootsocial.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProfileResponse {
    private User user;
    private List<Post> posts;
    private List<Post> savedPosts;
    private List<Reels> reels;
    private int postsCount;
    private int savedPostsCount;
    private int reelsCount;
    private int followersCount;
    private int followingCount;
}
