package com.bharat.springbootsocial.request;

import com.bharat.springbootsocial.entity.Story;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateStoryRequest {
    private String imageUrl;
    private String videoUrl;
    private String caption;
    private Story.StoryType storyType = Story.StoryType.IMAGE;
}
