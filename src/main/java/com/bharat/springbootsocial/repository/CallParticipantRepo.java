package com.bharat.springbootsocial.repository;

import com.bharat.springbootsocial.entity.CallParticipant;
import com.bharat.springbootsocial.entity.CallRoom;
import com.bharat.springbootsocial.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
import java.util.Optional;

@Repository
public interface CallParticipantRepo extends JpaRepository<CallParticipant, UUID> {
    
    List<CallParticipant> findByCallRoom(CallRoom callRoom);
    
    List<CallParticipant> findByUser(User user);
    
    Optional<CallParticipant> findByCallRoomAndUser(CallRoom callRoom, User user);
    
    List<CallParticipant> findByCallRoomAndStatus(CallRoom callRoom, CallParticipant.ParticipantStatus status);
    
    @Query("SELECT p FROM CallParticipant p WHERE p.callRoom = :room AND p.status = 'JOINED'")
    List<CallParticipant> findActiveParticipants(@Param("room") CallRoom room);
    
    @Query("SELECT COUNT(p) FROM CallParticipant p WHERE p.callRoom = :room AND p.status = 'JOINED'")
    Integer countActiveParticipantsInRoom(@Param("room") CallRoom room);
    
    @Query("SELECT p FROM CallParticipant p WHERE p.user = :user AND p.status = 'JOINED'")
    List<CallParticipant> findActiveParticipationsByUser(@Param("user") User user);
}
