package com.bharat.springbootsocial.repository;

import com.bharat.springbootsocial.entity.Message;
import com.bharat.springbootsocial.entity.MessageRead;
import com.bharat.springbootsocial.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MessageReadRepo extends JpaRepository<MessageRead, UUID> {
    
    /**
     * Find all read records for a specific message
     */
    List<MessageRead> findByMessage(Message message);
    
    /**
     * Find all read records for a specific message ID
     */
    List<MessageRead> findByMessageId(UUID messageId);
    
    /**
     * Find read record by message and user
     */
    Optional<MessageRead> findByMessageAndUser(Message message, User user);
    
    /**
     * Find read record by message ID and user ID
     */
    Optional<MessageRead> findByMessageIdAndUserId(UUID messageId, UUID userId);
    
    /**
     * Check if user has read a message
     */
    boolean existsByMessageIdAndUserId(UUID messageId, UUID userId);
    
    /**
     * Count reads for a specific message
     */
    long countByMessageId(UUID messageId);
    
    /**
     * Find all unread messages for a user in a specific chat
     */
    @Query("SELECT m FROM Message m WHERE m.chat.id = :chatId AND m.id NOT IN " +
           "(SELECT mr.message.id FROM MessageRead mr WHERE mr.user.id = :userId AND mr.message.chat.id = :chatId) " +
           "ORDER BY m.timestamp ASC")
    List<Message> findUnreadMessagesForUserInChat(@Param("userId") UUID userId, @Param("chatId") UUID chatId);
    
    /**
     * Count unread messages for a user in a specific chat
     */
    @Query("SELECT COUNT(m) FROM Message m WHERE m.chat.id = :chatId AND m.id NOT IN " +
           "(SELECT mr.message.id FROM MessageRead mr WHERE mr.user.id = :userId AND mr.message.chat.id = :chatId)")
    long countUnreadMessagesForUserInChat(@Param("userId") UUID userId, @Param("chatId") UUID chatId);
    
    /**
     * Find last read message for a user in a chat
     */
    @Query("SELECT mr FROM MessageRead mr WHERE mr.user.id = :userId AND mr.message.chat.id = :chatId " +
           "ORDER BY mr.readAt DESC LIMIT 1")
    Optional<MessageRead> findLastReadMessageForUserInChat(@Param("userId") UUID userId, @Param("chatId") UUID chatId);
    
    /**
     * Delete read record by message and user
     */
    void deleteByMessageAndUser(Message message, User user);
    
    /**
     * Delete read record by message ID and user ID
     */
    void deleteByMessageIdAndUserId(UUID messageId, UUID userId);
    
    /**
     * Delete all read records for a message
     */
    void deleteByMessageId(UUID messageId);
}
