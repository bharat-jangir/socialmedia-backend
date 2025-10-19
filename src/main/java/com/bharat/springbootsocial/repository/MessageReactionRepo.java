package com.bharat.springbootsocial.repository;

import com.bharat.springbootsocial.entity.Message;
import com.bharat.springbootsocial.entity.MessageReaction;
import com.bharat.springbootsocial.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MessageReactionRepo extends JpaRepository<MessageReaction, UUID> {
    
    /**
     * Find all reactions for a specific message
     */
    List<MessageReaction> findByMessage(Message message);
    
    /**
     * Find all reactions for a specific message ID
     */
    List<MessageReaction> findByMessageId(UUID messageId);
    
    /**
     * Find reaction by message and user
     */
    Optional<MessageReaction> findByMessageAndUser(Message message, User user);
    
    /**
     * Find reaction by message ID and user ID
     */
    Optional<MessageReaction> findByMessageIdAndUserId(UUID messageId, UUID userId);
    
    /**
     * Check if user has reacted to a message
     */
    boolean existsByMessageIdAndUserId(UUID messageId, UUID userId);
    
    /**
     * Count reactions for a specific message
     */
    long countByMessageId(UUID messageId);
    
    /**
     * Count reactions by emoji for a specific message
     */
    @Query("SELECT mr.emoji, COUNT(mr) FROM MessageReaction mr WHERE mr.message.id = :messageId GROUP BY mr.emoji")
    List<Object[]> countReactionsByEmoji(@Param("messageId") UUID messageId);
    
    /**
     * Delete reaction by message and user
     */
    void deleteByMessageAndUser(Message message, User user);
    
    /**
     * Delete reaction by message ID and user ID
     */
    void deleteByMessageIdAndUserId(UUID messageId, UUID userId);
    
    /**
     * Delete all reactions for a message
     */
    void deleteByMessageId(UUID messageId);
}
