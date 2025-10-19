package com.bharat.springbootsocial.repository;

import com.bharat.springbootsocial.entity.GroupMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.Optional;

@Repository
public interface GroupMessageRepo extends JpaRepository<GroupMessage, UUID> {
    
    // Find messages by group ID
    @Query("SELECT gm FROM GroupMessage gm WHERE gm.group.id = :groupId AND gm.isDeleted = false ORDER BY gm.createdAt DESC")
    List<GroupMessage> findMessagesByGroupId(@Param("groupId") UUID groupId);
    
    // Find messages by group ID with pagination
    @Query("SELECT gm FROM GroupMessage gm WHERE gm.group.id = :groupId AND gm.isDeleted = false ORDER BY gm.createdAt DESC")
    Page<GroupMessage> findMessagesByGroupIdPaginated(@Param("groupId") UUID groupId, Pageable pageable);
    
    // Find messages by group ID and message type
    @Query("SELECT gm FROM GroupMessage gm WHERE gm.group.id = :groupId AND gm.messageType = :messageType AND gm.isDeleted = false ORDER BY gm.createdAt DESC")
    List<GroupMessage> findMessagesByGroupIdAndType(@Param("groupId") UUID groupId, @Param("messageType") GroupMessage.MessageType messageType);
    
    // Find messages by sender
    @Query("SELECT gm FROM GroupMessage gm WHERE gm.sender.id = :senderId AND gm.isDeleted = false ORDER BY gm.createdAt DESC")
    List<GroupMessage> findMessagesBySenderId(@Param("senderId") UUID senderId);
    
    // Find messages by sender with pagination
    @Query("SELECT gm FROM GroupMessage gm WHERE gm.sender.id = :senderId AND gm.isDeleted = false ORDER BY gm.createdAt DESC")
    Page<GroupMessage> findMessagesBySenderIdPaginated(@Param("senderId") UUID senderId, Pageable pageable);
    
    // Find messages in date range
    @Query("SELECT gm FROM GroupMessage gm WHERE gm.group.id = :groupId AND gm.createdAt BETWEEN :startDate AND :endDate AND gm.isDeleted = false ORDER BY gm.createdAt DESC")
    List<GroupMessage> findMessagesByGroupIdAndDateRange(@Param("groupId") UUID groupId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    // Find system messages
    @Query("SELECT gm FROM GroupMessage gm WHERE gm.group.id = :groupId AND gm.messageType IN ('SYSTEM', 'CALL_START', 'CALL_END', 'MEMBER_JOINED', 'MEMBER_LEFT', 'GROUP_CREATED', 'GROUP_UPDATED') AND gm.isDeleted = false ORDER BY gm.createdAt DESC")
    List<GroupMessage> findSystemMessagesByGroupId(@Param("groupId") UUID groupId);
    
    // Find media messages (images, videos, files)
    @Query("SELECT gm FROM GroupMessage gm WHERE gm.group.id = :groupId AND gm.messageType IN ('IMAGE', 'VIDEO', 'FILE', 'AUDIO') AND gm.isDeleted = false ORDER BY gm.createdAt DESC")
    List<GroupMessage> findMediaMessagesByGroupId(@Param("groupId") UUID groupId);
    
    // Find media messages with pagination
    @Query("SELECT gm FROM GroupMessage gm WHERE gm.group.id = :groupId AND gm.messageType IN ('IMAGE', 'VIDEO', 'FILE', 'AUDIO') AND gm.isDeleted = false ORDER BY gm.createdAt DESC")
    Page<GroupMessage> findMediaMessagesByGroupIdPaginated(@Param("groupId") UUID groupId, Pageable pageable);
    
    // Find edited messages
    @Query("SELECT gm FROM GroupMessage gm WHERE gm.group.id = :groupId AND gm.isEdited = true AND gm.isDeleted = false ORDER BY gm.editedAt DESC")
    List<GroupMessage> findEditedMessagesByGroupId(@Param("groupId") UUID groupId);
    
    // Find messages with replies
    @Query("SELECT gm FROM GroupMessage gm WHERE gm.group.id = :groupId AND gm.replyTo IS NOT NULL AND gm.isDeleted = false ORDER BY gm.createdAt DESC")
    List<GroupMessage> findMessagesWithRepliesByGroupId(@Param("groupId") UUID groupId);
    
    // Find replies to a specific message
    @Query("SELECT gm FROM GroupMessage gm WHERE gm.replyTo.id = :messageId AND gm.isDeleted = false ORDER BY gm.createdAt ASC")
    List<GroupMessage> findRepliesByMessageId(@Param("messageId") UUID messageId);
    
    // Count messages by group
    @Query("SELECT COUNT(gm) FROM GroupMessage gm WHERE gm.group.id = :groupId AND gm.isDeleted = false")
    Long countMessagesByGroupId(@Param("groupId") UUID groupId);
    
    // Count messages by sender
    @Query("SELECT COUNT(gm) FROM GroupMessage gm WHERE gm.sender.id = :senderId AND gm.isDeleted = false")
    Long countMessagesBySenderId(@Param("senderId") UUID senderId);
    
    // Find latest message in group
    @Query("SELECT gm FROM GroupMessage gm WHERE gm.group.id = :groupId AND gm.isDeleted = false ORDER BY gm.createdAt DESC")
    Optional<GroupMessage> findLatestMessageByGroupId(@Param("groupId") UUID groupId);
    
    // Find messages before a specific date
    @Query("SELECT gm FROM GroupMessage gm WHERE gm.group.id = :groupId AND gm.createdAt < :beforeDate AND gm.isDeleted = false ORDER BY gm.createdAt DESC")
    List<GroupMessage> findMessagesBeforeDate(@Param("groupId") UUID groupId, @Param("beforeDate") LocalDateTime beforeDate);
    
    // Find messages after a specific date
    @Query("SELECT gm FROM GroupMessage gm WHERE gm.group.id = :groupId AND gm.createdAt > :afterDate AND gm.isDeleted = false ORDER BY gm.createdAt ASC")
    List<GroupMessage> findMessagesAfterDate(@Param("groupId") UUID groupId, @Param("afterDate") LocalDateTime afterDate);
    
    // Search messages by content
    @Query("SELECT gm FROM GroupMessage gm WHERE gm.group.id = :groupId AND gm.content LIKE %:query% AND gm.isDeleted = false ORDER BY gm.createdAt DESC")
    List<GroupMessage> searchMessagesByContent(@Param("groupId") UUID groupId, @Param("query") String query);
    
    // Search messages by content with pagination
    @Query("SELECT gm FROM GroupMessage gm WHERE gm.group.id = :groupId AND gm.content LIKE %:query% AND gm.isDeleted = false ORDER BY gm.createdAt DESC")
    Page<GroupMessage> searchMessagesByContentPaginated(@Param("groupId") UUID groupId, @Param("query") String query, Pageable pageable);
}

