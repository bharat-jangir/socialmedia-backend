package com.bharat.springbootsocial.repository;

import com.bharat.springbootsocial.entity.GroupCallRoom;
import com.bharat.springbootsocial.entity.GroupCallSession;
import com.bharat.springbootsocial.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GroupCallSessionRepo extends JpaRepository<GroupCallSession, UUID> {
    
    // Find session by room and user
    Optional<GroupCallSession> findByGroupCallRoomAndUser(GroupCallRoom groupCallRoom, User user);
    
    // Find all sessions for a room
    List<GroupCallSession> findByGroupCallRoom(GroupCallRoom groupCallRoom);
    
    // Find all sessions for a user
    List<GroupCallSession> findByUser(User user);
    
    // Find active sessions for a room
    @Query("SELECT s FROM GroupCallSession s WHERE s.groupCallRoom = :room AND s.status IN ('JOINING', 'CONNECTED')")
    List<GroupCallSession> findActiveSessionsByRoom(@Param("room") GroupCallRoom room);
    
    // Find active sessions for a user
    @Query("SELECT s FROM GroupCallSession s WHERE s.user = :user AND s.status IN ('JOINING', 'CONNECTED')")
    List<GroupCallSession> findActiveSessionsByUser(@Param("user") User user);
    
    // Count active sessions in a room
    @Query("SELECT COUNT(s) FROM GroupCallSession s WHERE s.groupCallRoom = :room AND s.status IN ('JOINING', 'CONNECTED')")
    long countActiveSessionsByRoom(@Param("room") GroupCallRoom room);
    
    // Find sessions by room ID
    @Query("SELECT s FROM GroupCallSession s WHERE s.groupCallRoom.id = :roomId")
    List<GroupCallSession> findByGroupCallRoomId(@Param("roomId") UUID roomId);
    
    // Find active sessions by room ID
    @Query("SELECT s FROM GroupCallSession s WHERE s.groupCallRoom.id = :roomId AND s.status IN ('JOINING', 'CONNECTED')")
    List<GroupCallSession> findActiveSessionsByGroupCallRoomId(@Param("roomId") UUID roomId);
    
    // Delete sessions by room
    void deleteByGroupCallRoom(GroupCallRoom groupCallRoom);
    
    // Delete sessions by room ID
    @Query("DELETE FROM GroupCallSession s WHERE s.groupCallRoom.id = :roomId")
    void deleteByGroupCallRoomId(@Param("roomId") UUID roomId);
}
