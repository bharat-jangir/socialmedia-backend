package com.bharat.springbootsocial.repository;

import com.bharat.springbootsocial.entity.CallRoom;
import com.bharat.springbootsocial.entity.CallSession;
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
public interface CallSessionRepo extends JpaRepository<CallSession, UUID> {
    
    // Find session by room and user
    Optional<CallSession> findByRoomAndUser(CallRoom room, User user);
    
    // Find all sessions in a room
    List<CallSession> findByRoom(CallRoom room);
    
    // Find active sessions in a room
    @Query("SELECT cs FROM CallSession cs WHERE cs.room = :room AND cs.status IN ('JOINING', 'CONNECTED')")
    List<CallSession> findActiveSessionsByRoom(@Param("room") CallRoom room);
    
    // Find sessions by user
    List<CallSession> findByUser(User user);
    
    // Find active sessions by user
    @Query("SELECT cs FROM CallSession cs WHERE cs.user = :user AND cs.status IN ('JOINING', 'CONNECTED')")
    List<CallSession> findActiveSessionsByUser(@Param("user") User user);
    
    // Find sessions by status
    List<CallSession> findByStatus(CallSession.SessionStatus status);
    
    // Count active sessions in room
    @Query("SELECT COUNT(cs) FROM CallSession cs WHERE cs.room = :room AND cs.status IN ('JOINING', 'CONNECTED')")
    long countActiveSessionsByRoom(@Param("room") CallRoom room);
    
    // Find sessions that need cleanup (disconnected more than 30 minutes ago)
    @Query("SELECT cs FROM CallSession cs WHERE cs.status = 'DISCONNECTED' AND cs.leftAt < :cutoffTime")
    List<CallSession> findSessionsForCleanup(@Param("cutoffTime") LocalDateTime cutoffTime);
    
    // Find sessions by connection state
    List<CallSession> findByConnectionState(String connectionState);
    
    // Find sessions by ICE connection state
    List<CallSession> findByIceConnectionState(String iceConnectionState);
    
    // Find sessions with recent activity
    @Query("SELECT cs FROM CallSession cs WHERE cs.lastActivity > :since")
    List<CallSession> findSessionsWithRecentActivity(@Param("since") LocalDateTime since);
    
    // Find sessions by room ID
    @Query("SELECT cs FROM CallSession cs WHERE cs.room.roomId = :roomId")
    List<CallSession> findByRoomId(@Param("roomId") String roomId);
    
    // Find active sessions by room ID
    @Query("SELECT cs FROM CallSession cs WHERE cs.room.roomId = :roomId AND cs.status IN ('JOINING', 'CONNECTED')")
    List<CallSession> findActiveSessionsByRoomId(@Param("roomId") String roomId);
}