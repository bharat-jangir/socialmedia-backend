package com.bharat.springbootsocial.repository;

import com.bharat.springbootsocial.entity.GroupMessageReaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
import java.util.Optional;

@Repository
public interface GroupMessageReactionRepo extends JpaRepository<GroupMessageReaction, UUID> {
    
    // Find reactions by message ID
    @Query("SELECT gmr FROM GroupMessageReaction gmr WHERE gmr.message.id = :messageId")
    List<GroupMessageReaction> findReactionsByMessageId(@Param("messageId") UUID messageId);
    
    // Find reactions by message ID and emoji
    @Query("SELECT gmr FROM GroupMessageReaction gmr WHERE gmr.message.id = :messageId AND gmr.emoji = :emoji")
    List<GroupMessageReaction> findReactionsByMessageIdAndEmoji(@Param("messageId") UUID messageId, @Param("emoji") String emoji);
    
    // Find reaction by message ID and user ID
    @Query("SELECT gmr FROM GroupMessageReaction gmr WHERE gmr.message.id = :messageId AND gmr.user.id = :userId")
    Optional<GroupMessageReaction> findReactionByMessageIdAndUserId(@Param("messageId") UUID messageId, @Param("userId") UUID userId);
    
    // Find reaction by message ID, user ID, and emoji
    @Query("SELECT gmr FROM GroupMessageReaction gmr WHERE gmr.message.id = :messageId AND gmr.user.id = :userId AND gmr.emoji = :emoji")
    Optional<GroupMessageReaction> findReactionByMessageIdAndUserIdAndEmoji(@Param("messageId") UUID messageId, @Param("userId") UUID userId, @Param("emoji") String emoji);
    
    // Find reactions by user ID
    @Query("SELECT gmr FROM GroupMessageReaction gmr WHERE gmr.user.id = :userId")
    List<GroupMessageReaction> findReactionsByUserId(@Param("userId") UUID userId);
    
    // Count reactions by message ID
    @Query("SELECT COUNT(gmr) FROM GroupMessageReaction gmr WHERE gmr.message.id = :messageId")
    Long countReactionsByMessageId(@Param("messageId") UUID messageId);
    
    // Count reactions by message ID and emoji
    @Query("SELECT COUNT(gmr) FROM GroupMessageReaction gmr WHERE gmr.message.id = :messageId AND gmr.emoji = :emoji")
    Long countReactionsByMessageIdAndEmoji(@Param("messageId") UUID messageId, @Param("emoji") String emoji);
    
    // Check if user has reacted to message
    @Query("SELECT COUNT(gmr) > 0 FROM GroupMessageReaction gmr WHERE gmr.message.id = :messageId AND gmr.user.id = :userId")
    boolean hasUserReactedToMessage(@Param("messageId") UUID messageId, @Param("userId") UUID userId);
    
    // Check if user has reacted with specific emoji
    @Query("SELECT COUNT(gmr) > 0 FROM GroupMessageReaction gmr WHERE gmr.message.id = :messageId AND gmr.user.id = :userId AND gmr.emoji = :emoji")
    boolean hasUserReactedWithEmoji(@Param("messageId") UUID messageId, @Param("userId") UUID userId, @Param("emoji") String emoji);
    
    // Find all unique emojis used in a message
    @Query("SELECT DISTINCT gmr.emoji FROM GroupMessageReaction gmr WHERE gmr.message.id = :messageId")
    List<String> findUniqueEmojisByMessageId(@Param("messageId") UUID messageId);
    
    // Find reactions by emoji across all messages
    @Query("SELECT gmr FROM GroupMessageReaction gmr WHERE gmr.emoji = :emoji")
    List<GroupMessageReaction> findReactionsByEmoji(@Param("emoji") String emoji);
}

