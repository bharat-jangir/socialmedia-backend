package com.bharat.springbootsocial.services;

import com.bharat.springbootsocial.entity.Story;
import com.bharat.springbootsocial.entity.User;
import com.bharat.springbootsocial.exception.UserException;
import com.bharat.springbootsocial.request.CreateStoryRequest;
import com.bharat.springbootsocial.request.StoryReplyRequest;
import com.bharat.springbootsocial.response.PaginatedStoryResponse;
import com.bharat.springbootsocial.response.StoryResponse;
import com.bharat.springbootsocial.response.UserStoryResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface StoryService {
    // Story CRUD operations
    StoryResponse createStory(CreateStoryRequest request, User user);
    StoryResponse getStoryById(UUID storyId, User currentUser) throws UserException;
    List<StoryResponse> getActiveStoriesByUserId(UUID userId, User currentUser);
    List<UserStoryResponse> getStoriesFromFollowing(User currentUser);
    PaginatedStoryResponse getStoriesFromFollowingPaginated(User currentUser, Pageable pageable);
    void deleteStory(UUID storyId, User user) throws UserException;
    
    // Story interactions
    StoryResponse viewStory(UUID storyId, User user) throws UserException;
    StoryResponse likeStory(UUID storyId, User user) throws UserException;
    StoryResponse unlikeStory(UUID storyId, User user) throws UserException;
    StoryResponse replyToStory(StoryReplyRequest request, User user) throws UserException;
    
    // Story management
    void deactivateExpiredStories();
    List<StoryResponse> getUnreadReplies(User user);
    void markReplyAsRead(UUID replyId, User user) throws UserException;
    
    // Story analytics
    Integer getStoryViewCount(UUID storyId) throws UserException;
    Integer getStoryLikeCount(UUID storyId) throws UserException;
    Integer getStoryReplyCount(UUID storyId);
}
