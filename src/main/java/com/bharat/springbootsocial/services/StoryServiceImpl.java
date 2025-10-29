package com.bharat.springbootsocial.services;

import com.bharat.springbootsocial.entity.Story;
import com.bharat.springbootsocial.entity.StoryReply;
import com.bharat.springbootsocial.entity.User;
import com.bharat.springbootsocial.exception.UserException;
import com.bharat.springbootsocial.repository.StoryRepo;
import com.bharat.springbootsocial.repository.StoryReplyRepo;
import com.bharat.springbootsocial.request.CreateStoryRequest;
import com.bharat.springbootsocial.request.StoryReplyRequest;
import com.bharat.springbootsocial.response.PaginatedStoryResponse;
import com.bharat.springbootsocial.response.StoryResponse;
import com.bharat.springbootsocial.response.UserStoryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class StoryServiceImpl implements StoryService {
    
    @Autowired
    private StoryRepo storyRepo;
    
    @Autowired
    private StoryReplyRepo storyReplyRepo;

    @Autowired
    private ServiceInt userService;
    
    @Autowired
    private NotificationService notificationService;
    
    private static final int STORY_EXPIRY_HOURS = 24;
    
    @Override
    @Transactional
    public StoryResponse createStory(CreateStoryRequest request, User user) {
        Story story = new Story();
        story.setUser(user);
        story.setImageUrl(request.getImageUrl());
        story.setVideoUrl(request.getVideoUrl());
        story.setCaption(request.getCaption());
        story.setStoryType(request.getStoryType());
        story.setCreatedAt(LocalDateTime.now());
        story.setExpiresAt(LocalDateTime.now().plusHours(STORY_EXPIRY_HOURS));
        story.setIsActive(true);
        story.setViewCount(0);
        
        Story savedStory = storyRepo.save(story);
        return convertToStoryResponse(savedStory, user);
    }
    
    @Override
    public StoryResponse getStoryById(UUID storyId, User currentUser) throws UserException {
        Story story = storyRepo.findById(storyId)
                .orElseThrow(() -> new UserException("Story not found"));
        
        if (!story.getIsActive() || story.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new UserException("Story has expired or is not active");
        }
        
        return convertToStoryResponse(story, currentUser);
    }

    @Override
    public List<StoryResponse> getActiveStoriesByUserId(UUID userId, User currentUser) {
        userService.getUserById(userId);
        List<Story> stories = storyRepo.findActiveStoriesByUserId(userId, LocalDateTime.now());
        
        return stories.stream()
                .map(story -> convertToStoryResponse(story, currentUser))
                .collect(Collectors.toList());
    }
    
    @Override
    public List<UserStoryResponse> getStoriesFromFollowing(User currentUser) {
        List<String> followingIds = currentUser.getFollowing();
        if (followingIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<Story> stories = storyRepo.findActiveStoriesFromFollowing(followingIds, LocalDateTime.now());
        
        // Group stories by user
        Map<UUID, List<Story>> storiesByUser = stories.stream()
                .collect(Collectors.groupingBy(story -> story.getUser().getId()));
        
        List<UserStoryResponse> userStoryResponses = new ArrayList<>();
        
        for (Map.Entry<UUID, List<Story>> entry : storiesByUser.entrySet()) {
            UUID userId = entry.getKey();
            List<Story> userStories = entry.getValue();
            
            User storyUser = userStories.get(0).getUser();
            List<StoryResponse> storyResponses = userStories.stream()
                    .map(story -> convertToStoryResponse(story, currentUser))
                    .collect(Collectors.toList());
            
            // Check if user has unviewed stories
            Integer unviewedCount = storyRepo.countUnviewedStoriesByUserId(userId, currentUser, LocalDateTime.now());
            Boolean hasUnviewedStories = unviewedCount > 0;
            
            UserStoryResponse userStoryResponse = new UserStoryResponse();
            userStoryResponse.setUser(storyUser);
            userStoryResponse.setStories(storyResponses);
            userStoryResponse.setHasUnviewedStories(hasUnviewedStories);
            userStoryResponse.setTotalStories(userStories.size());
            
            userStoryResponses.add(userStoryResponse);
        }
        
        return userStoryResponses;
    }
    
    @Override
    public PaginatedStoryResponse getStoriesFromFollowingPaginated(User currentUser, Pageable pageable) {
        List<String> followingIds = currentUser.getFollowing();
        if (followingIds.isEmpty()) {
            return new PaginatedStoryResponse();
        }
        
        // Get paginated stories from following users
        Page<Story> storyPage = storyRepo.findActiveStoriesFromFollowingPaginated(followingIds, LocalDateTime.now(), pageable);
        
        // Group stories by user
        Map<UUID, List<Story>> storiesByUser = storyPage.getContent().stream()
                .collect(Collectors.groupingBy(story -> story.getUser().getId()));
        
        List<UserStoryResponse> userStoryResponses = new ArrayList<>();
        
        for (Map.Entry<UUID, List<Story>> entry : storiesByUser.entrySet()) {
            UUID userId = entry.getKey();
            List<Story> userStories = entry.getValue();
            
            User storyUser = userStories.get(0).getUser();
            List<StoryResponse> storyResponses = userStories.stream()
                    .map(story -> convertToStoryResponse(story, currentUser))
                    .collect(Collectors.toList());
            
            // Check if user has unviewed stories
            Integer unviewedCount = storyRepo.countUnviewedStoriesByUserId(userId, currentUser, LocalDateTime.now());
            Boolean hasUnviewedStories = unviewedCount > 0;
            
            UserStoryResponse userStoryResponse = new UserStoryResponse();
            userStoryResponse.setUser(storyUser);
            userStoryResponse.setStories(storyResponses);
            userStoryResponse.setHasUnviewedStories(hasUnviewedStories);
            userStoryResponse.setTotalStories(userStories.size());
            
            userStoryResponses.add(userStoryResponse);
        }
        
        // Create paginated response
        PaginatedStoryResponse paginatedResponse = new PaginatedStoryResponse();
        paginatedResponse.setUserStories(userStoryResponses);
        paginatedResponse.setCurrentPage(storyPage.getNumber());
        paginatedResponse.setTotalPages(storyPage.getTotalPages());
        paginatedResponse.setTotalElements(storyPage.getTotalElements());
        paginatedResponse.setPageSize(storyPage.getSize());
        paginatedResponse.setHasNext(storyPage.hasNext());
        paginatedResponse.setHasPrevious(storyPage.hasPrevious());
        paginatedResponse.setFirst(storyPage.isFirst());
        paginatedResponse.setLast(storyPage.isLast());
        
        return paginatedResponse;
    }
    
    @Override
    @Transactional
    public void deleteStory(UUID storyId, User user) throws UserException {
        Story story = storyRepo.findById(storyId)
                .orElseThrow(() -> new UserException("Story not found"));
        
        if (!story.getUser().getId().equals(user.getId())) {
            throw new UserException("You can only delete your own stories");
        }
        
        storyRepo.delete(story);
    }
    
    @Override
    @Transactional
    public StoryResponse viewStory(UUID storyId, User user) throws UserException {
        Story story = storyRepo.findById(storyId)
                .orElseThrow(() -> new UserException("Story not found"));
        
        if (!story.getIsActive() || story.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new UserException("Story has expired or is not active");
        }
        
        // Add user to viewedBy list if not already viewed
        if (!story.getViewedBy().contains(user)) {
            story.getViewedBy().add(user);
            story.setViewCount(story.getViewCount() + 1);
            storyRepo.save(story);
            
            // Story view notification removed as per user request
        }
        
        return convertToStoryResponse(story, user);
    }
    
    @Override
    @Transactional
    public StoryResponse likeStory(UUID storyId, User user) throws UserException {
        Story story = storyRepo.findById(storyId)
                .orElseThrow(() -> new UserException("Story not found"));
        
        if (!story.getIsActive() || story.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new UserException("Story has expired or is not active");
        }
        
        if (!story.getLikedBy().contains(user)) {
            story.getLikedBy().add(user);
            storyRepo.save(story);
            
            // Send like notification
            notificationService.sendLikeNotification(story.getUser(), user, "STORY", story.getId());
        }
        
        return convertToStoryResponse(story, user);
    }
    
    @Override
    @Transactional
    public StoryResponse unlikeStory(UUID storyId, User user) throws UserException {
        Story story = storyRepo.findById(storyId)
                .orElseThrow(() -> new UserException("Story not found"));
        
        story.getLikedBy().remove(user);
        storyRepo.save(story);
        
        return convertToStoryResponse(story, user);
    }
    
    @Override
    @Transactional
    public StoryResponse replyToStory(StoryReplyRequest request, User user) throws UserException {
        Story story = storyRepo.findById(request.getStoryId())
                .orElseThrow(() -> new UserException("Story not found"));
        
        if (!story.getIsActive() || story.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new UserException("Story has expired or is not active");
        }
        
        StoryReply reply = new StoryReply();
        reply.setStory(story);
        reply.setUser(user);
        reply.setReplyText(request.getReplyText());
        reply.setCreatedAt(LocalDateTime.now());
        reply.setIsRead(false);
        
        storyReplyRepo.save(reply);
        
        // Send comment notification
        notificationService.sendCommentNotification(story.getUser(), user, "STORY", story.getId());
        
        return convertToStoryResponse(story, user);
    }
    
    @Override
    @Scheduled(fixedRate = 3600000) // Run every hour
    @Transactional
    public void deactivateExpiredStories() {
        List<Story> expiredStories = storyRepo.findStoriesToDeactivate(LocalDateTime.now());
        for (Story story : expiredStories) {
            story.setIsActive(false);
        }
        storyRepo.saveAll(expiredStories);
    }
    
    @Override
    public List<StoryResponse> getUnreadReplies(User user) {
        List<StoryReply> unreadReplies = storyReplyRepo.findUnreadRepliesByUserId(user.getId());
        
        return unreadReplies.stream()
                .map(reply -> convertToStoryResponse(reply.getStory(), user))
                .distinct()
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public void markReplyAsRead(UUID replyId, User user) throws UserException {
        StoryReply reply = storyReplyRepo.findById(replyId)
                .orElseThrow(() -> new UserException("Reply not found"));
        
        if (!reply.getStory().getUser().getId().equals(user.getId())) {
            throw new UserException("You can only mark replies to your own stories as read");
        }
        
        reply.setIsRead(true);
        storyReplyRepo.save(reply);
    }
    
    @Override
    public Integer getStoryViewCount(UUID storyId) throws UserException {
        Story story = storyRepo.findById(storyId)
                .orElseThrow(() -> new UserException("Story not found"));
        return story.getViewCount();
    }
    
    @Override
    public Integer getStoryLikeCount(UUID storyId) throws UserException {
        Story story = storyRepo.findById(storyId)
                .orElseThrow(() -> new UserException("Story not found"));
        return story.getLikedBy().size();
    }
    
    @Override
    public Integer getStoryReplyCount(UUID storyId) {
        return storyReplyRepo.countRepliesByStoryId(storyId);
    }
    
    private StoryResponse convertToStoryResponse(Story story, User currentUser) {
        StoryResponse response = new StoryResponse();
        response.setId(story.getId());
        response.setImageUrl(story.getImageUrl());
        response.setVideoUrl(story.getVideoUrl());
        response.setCaption(story.getCaption());
        response.setCreatedAt(story.getCreatedAt());
        response.setExpiresAt(story.getExpiresAt());
        response.setIsActive(story.getIsActive());
        response.setStoryType(story.getStoryType());
        response.setViewCount(story.getViewCount());
        response.setLikeCount(story.getLikedBy().size());
        response.setReplyCount(story.getReplies().size());
        response.setIsViewed(story.getViewedBy().contains(currentUser));
        response.setIsLiked(story.getLikedBy().contains(currentUser));
        
        // Convert replies
        List<StoryResponse.StoryReplyResponse> replyResponses = story.getReplies().stream()
                .map(reply -> {
                    StoryResponse.StoryReplyResponse replyResponse = new StoryResponse.StoryReplyResponse();
                    replyResponse.setId(reply.getId());
                    replyResponse.setUser(reply.getUser());
                    replyResponse.setReplyText(reply.getReplyText());
                    replyResponse.setCreatedAt(reply.getCreatedAt());
                    replyResponse.setIsRead(reply.getIsRead());
                    return replyResponse;
                })
                .collect(Collectors.toList());
        
        response.setReplies(replyResponses);
        
        return response;
    }
}
