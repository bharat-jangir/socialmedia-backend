package com.bharat.springbootsocial.response;

import com.bharat.springbootsocial.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserStoryResponse {
    private User user;
    private List<StoryResponse> stories;
    private Boolean hasUnviewedStories;
    private Integer totalStories;
}
