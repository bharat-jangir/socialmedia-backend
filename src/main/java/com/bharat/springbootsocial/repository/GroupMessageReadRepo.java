package com.bharat.springbootsocial.repository;

import com.bharat.springbootsocial.entity.GroupMessageRead;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.Optional;

@Repository
public interface GroupMessageReadRepo extends JpaRepository<GroupMessageRead, UUID> {
    
    // Find read status by message ID
    @Query("SELECT gmr FROM GroupMessageRead gmr WHERE gmr.message.id = :messageId")
    List<GroupMessageRead> findReadsByMessageId(@Param("messageId") UUID messageId);
    
    // Find read status by message ID and user ID
    @Query("SELECT gmr FROM GroupMessageRead gmr WHERE gmr.message.id = :messageId AND gmr.user.id = :userId")
    Optional<GroupMessageRead> findReadByMessageIdAndUserId(@Param("messageId") UUID messageId, @Param("userId") UUID userId);
    
    // Find read status by user ID
    @Query("SELECT gmr FROM GroupMessageRead gmr WHERE gmr.user.id = :userId")
    List<GroupMessageRead> findReadsByUserId(@Param("userId") UUID userId);
    
    // Count reads by message ID
    @Query("SELECT COUNT(gmr) FROM GroupMessageRead gmr WHERE gmr.message.id = :messageId")
    Long countReadsByMessageId(@Param("messageId") UUID messageId);
    
    // Check if user has read message
    @Query("SELECT COUNT(gmr) > 0 FROM GroupMessageRead gmr WHERE gmr.message.id = :messageId AND gmr.user.id = :userId")
    boolean hasUserReadMessage(@Param("messageId") UUID messageId, @Param("userId") UUID userId);
    
    // Find unread messages for user in group
    @Query("SELECT gm FROM GroupMessage gm WHERE gm.group.id = :groupId AND gm.isDeleted = false AND gm.id NOT IN " +
           "(SELECT gmr.message.id FROM GroupMessageRead gmr WHERE gmr.user.id = :userId) " +
           "ORDER BY gm.createdAt DESC")
    List<com.bharat.springbootsocial.entity.GroupMessage> findUnreadMessagesForUserInGroup(@Param("groupId") UUID groupId, @Param("userId") UUID userId);
    
    // Count unread messages for user in group
    @Query("SELECT COUNT(gm) FROM GroupMessage gm WHERE gm.group.id = :groupId AND gm.isDeleted = false AND gm.id NOT IN " +
           "(SELECT gmr.message.id FROM GroupMessageRead gmr WHERE gmr.user.id = :userId)")
    Long countUnreadMessagesForUserInGroup(@Param("groupId") UUID groupId, @Param("userId") UUID userId);
    
    // Find last read message for user in group
    @Query("SELECT gmr FROM GroupMessageRead gmr WHERE gmr.user.id = :userId AND gmr.message.group.id = :groupId " +
           "ORDER BY gmr.readAt DESC")
    Optional<GroupMessageRead> findLastReadMessageForUserInGroup(@Param("userId") UUID userId, @Param("groupId") UUID groupId);
    
    // Find read status after specific date
    @Query("SELECT gmr FROM GroupMessageRead gmr WHERE gmr.user.id = :userId AND gmr.readAt > :afterDate")
    List<GroupMessageRead> findReadsAfterDate(@Param("userId") UUID userId, @Param("afterDate") LocalDateTime afterDate);
    
    // Find read status before specific date
    @Query("SELECT gmr FROM GroupMessageRead gmr WHERE gmr.user.id = :userId AND gmr.readAt < :beforeDate")
    List<GroupMessageRead> findReadsBeforeDate(@Param("userId") UUID userId, @Param("beforeDate") LocalDateTime beforeDate);
}

