package com.bharat.springbootsocial.repository;

import com.bharat.springbootsocial.entity.CallRoom;
import com.bharat.springbootsocial.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.Optional;

@Repository
public interface CallRoomRepo extends JpaRepository<CallRoom, UUID> {
    
    // Find room by roomId
    Optional<CallRoom> findByRoomId(String roomId);
    
    // Find active rooms
    List<CallRoom> findByIsActiveTrue();
    
    // Find rooms by status
    List<CallRoom> findByStatus(CallRoom.CallStatus status);
    
    // Find rooms created by user
    List<CallRoom> findByCreatedBy(User user);
    
    // Find rooms where user is participant
    @Query("SELECT cr FROM CallRoom cr JOIN cr.participants p WHERE p = :user AND cr.isActive = true")
    List<CallRoom> findActiveRoomsByParticipant(@Param("user") User user);
    
    // Find rooms by call type
    List<CallRoom> findByCallType(CallRoom.CallType callType);
    
    // Find rooms created after specific time
    List<CallRoom> findByCreatedAtAfter(LocalDateTime after);
    
    // Find rooms by creator and status
    List<CallRoom> findByCreatedByAndStatus(User createdBy, CallRoom.CallStatus status);
    
    // Check if room exists and is active
    boolean existsByRoomIdAndIsActiveTrue(String roomId);
    
    // Count active rooms
    long countByIsActiveTrue();
    
    // Find rooms that need cleanup (ended more than 1 hour ago)
    @Query("SELECT cr FROM CallRoom cr WHERE cr.status = 'ENDED' AND cr.endedAt < :cutoffTime")
    List<CallRoom> findRoomsForCleanup(@Param("cutoffTime") LocalDateTime cutoffTime);
    
    // Find rooms by participant count
    @Query("SELECT cr FROM CallRoom cr WHERE SIZE(cr.participants) = :count")
    List<CallRoom> findByParticipantCount(@Param("count") int count);
    
    // Find rooms with specific participant
    @Query("SELECT cr FROM CallRoom cr JOIN cr.participants p WHERE p.id = :userId AND cr.isActive = true")
    List<CallRoom> findActiveRoomsByUserId(@Param("userId") UUID userId);
}