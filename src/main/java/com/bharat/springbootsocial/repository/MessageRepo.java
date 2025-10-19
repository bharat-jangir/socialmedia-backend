package com.bharat.springbootsocial.repository;

import com.bharat.springbootsocial.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface MessageRepo extends JpaRepository<Message,UUID> {
    
    /**
     * Find all messages for a specific chat
     */
    List<Message> findByChatId(UUID chatId);
    
    /**
     * Find messages for a specific chat with pagination, ordered by timestamp descending
     */
    Page<Message> findByChatIdOrderByTimestampDesc(UUID chatId, Pageable pageable);
    
    /**
     * Find messages for a specific chat with pagination, ordered by timestamp ascending
     */
    Page<Message> findByChatIdOrderByTimestampAsc(UUID chatId, Pageable pageable);
    
    /**
     * Find messages by user
     */
    List<Message> findByUserId(UUID userId);
    
    /**
     * Find messages by user and chat
     */
    List<Message> findByUserIdAndChatId(UUID userId, UUID chatId);
    
    /**
     * Find messages by message type
     */
    List<Message> findByMessageType(Message.MessageType messageType);
    
    /**
     * Find messages by chat and message type
     */
    List<Message> findByChatIdAndMessageType(UUID chatId, Message.MessageType messageType);
    
    /**
     * Find edited messages
     */
    List<Message> findByIsEditedTrue();
    
    /**
     * Find deleted messages
     */
    List<Message> findByIsDeletedTrue();
    
    /**
     * Find messages with replies
     */
    @Query("SELECT m FROM Message m WHERE m.replyTo IS NOT NULL")
    List<Message> findMessagesWithReplies();
    
    /**
     * Find replies to a specific message
     */
    List<Message> findByReplyToId(UUID replyToId);
    
    /**
     * Count messages in a chat
     */
    long countByChatId(UUID chatId);
    
    /**
     * Count messages by user in a chat
     */
    long countByUserIdAndChatId(UUID userId, UUID chatId);
    
    /**
     * Find latest message in a chat
     */
    @Query("SELECT m FROM Message m WHERE m.chat.id = :chatId ORDER BY m.timestamp DESC LIMIT 1")
    Message findLatestMessageInChat(@Param("chatId") UUID chatId);
    
    /**
     * Find messages after a specific timestamp
     */
    @Query("SELECT m FROM Message m WHERE m.chat.id = :chatId AND m.timestamp > :timestamp ORDER BY m.timestamp ASC")
    List<Message> findMessagesAfterTimestamp(@Param("chatId") UUID chatId, @Param("timestamp") java.time.LocalDateTime timestamp);
    
    /**
     * Find messages before a specific timestamp
     */
    @Query("SELECT m FROM Message m WHERE m.chat.id = :chatId AND m.timestamp < :timestamp ORDER BY m.timestamp DESC")
    List<Message> findMessagesBeforeTimestamp(@Param("chatId") UUID chatId, @Param("timestamp") java.time.LocalDateTime timestamp);
}
