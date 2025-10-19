package com.bharat.springbootsocial.repository;

import com.bharat.springbootsocial.entity.Group;
import com.bharat.springbootsocial.entity.GroupCallRoom;
import com.bharat.springbootsocial.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GroupCallRoomRepo extends JpaRepository<GroupCallRoom, UUID> {
    
    // Find by room ID
    Optional<GroupCallRoom> findByRoomId(String roomId);
    
    // Find by group ID
    List<GroupCallRoom> findByGroupId(UUID groupId);
    
    // Find active rooms by group ID
    List<GroupCallRoom> findByGroupIdAndIsActiveTrue(UUID groupId);
    
    // Find by group entity
    List<GroupCallRoom> findByGroup(Group group);
    
    // Find active rooms by group entity
    List<GroupCallRoom> findByGroupAndIsActiveTrue(Group group);
    
    // Find by status
    List<GroupCallRoom> findByStatus(GroupCallRoom.CallStatus status);
    
    // Find by call type
    List<GroupCallRoom> findByCallType(GroupCallRoom.CallType callType);
    
    // Find by creator
    List<GroupCallRoom> findByCreatedBy(User createdBy);
    
    // Find by participants
    List<GroupCallRoom> findByParticipantsContaining(User participant);
    
    // Find active rooms by participant
    List<GroupCallRoom> findByParticipantsContainingAndIsActiveTrue(User participant);
    
    // Find by group and status
    List<GroupCallRoom> findByGroupIdAndStatus(UUID groupId, GroupCallRoom.CallStatus status);
    
    // Find by group and call type
    List<GroupCallRoom> findByGroupIdAndCallType(UUID groupId, GroupCallRoom.CallType callType);
    
    // Check if room exists by room ID
    boolean existsByRoomId(String roomId);
    
    // Count active rooms by group
    long countByGroupIdAndIsActiveTrue(UUID groupId);
    
    // Count rooms by status
    long countByStatus(GroupCallRoom.CallStatus status);
    
    // Count call rooms by creator
    long countByCreatedBy(User createdBy);
    
    // Count call rooms by group
    long countByGroup(Group group);
}