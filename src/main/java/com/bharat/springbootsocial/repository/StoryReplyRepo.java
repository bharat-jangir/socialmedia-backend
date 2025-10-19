package com.bharat.springbootsocial.repository;

import com.bharat.springbootsocial.entity.StoryReply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface StoryReplyRepo extends JpaRepository<StoryReply, UUID> {
    List<StoryReply> findByStoryId(UUID storyId);
    
    @Query("SELECT sr FROM StoryReply sr WHERE sr.story.user.id = :userId AND sr.isRead = false")
    List<StoryReply> findUnreadRepliesByUserId(@Param("userId") UUID userId);
    
    @Query("SELECT COUNT(sr) FROM StoryReply sr WHERE sr.story.id = :storyId")
    Integer countRepliesByStoryId(@Param("storyId") UUID storyId);
}
