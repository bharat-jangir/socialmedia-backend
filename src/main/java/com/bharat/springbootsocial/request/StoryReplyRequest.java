package com.bharat.springbootsocial.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StoryReplyRequest {
    private UUID storyId;
    private String replyText;
}
