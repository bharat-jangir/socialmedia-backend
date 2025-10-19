package com.bharat.springbootsocial.repository;

import com.bharat.springbootsocial.entity.WebRTCSignal;
import com.bharat.springbootsocial.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface WebRTCSignalRepo extends JpaRepository<WebRTCSignal, UUID> {
    
    List<WebRTCSignal> findByRoomId(String roomId);
    
    List<WebRTCSignal> findByFromUser(User fromUser);
    
    List<WebRTCSignal> findByToUser(User toUser);
    
    List<WebRTCSignal> findByRoomIdAndSignalType(String roomId, WebRTCSignal.SignalType signalType);
    
    @Query("SELECT s FROM WebRTCSignal s WHERE s.roomId = :roomId AND s.isProcessed = false ORDER BY s.createdAt ASC")
    List<WebRTCSignal> findUnprocessedSignalsByRoom(@Param("roomId") String roomId);
    
    @Query("SELECT s FROM WebRTCSignal s WHERE s.toUser = :user AND s.isProcessed = false ORDER BY s.createdAt ASC")
    List<WebRTCSignal> findUnprocessedSignalsForUser(@Param("user") User user);
    
    @Query("SELECT s FROM WebRTCSignal s WHERE s.createdAt < :cutoffTime")
    List<WebRTCSignal> findOldSignals(@Param("cutoffTime") LocalDateTime cutoffTime);
    
    @Query("SELECT COUNT(s) FROM WebRTCSignal s WHERE s.roomId = :roomId AND s.signalType = :signalType")
    Long countSignalsByRoomAndType(@Param("roomId") String roomId, @Param("signalType") WebRTCSignal.SignalType signalType);
}
