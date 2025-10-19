package com.bharat.springbootsocial.repository;

import com.bharat.springbootsocial.entity.GroupMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GroupMemberRepo extends JpaRepository<GroupMember, UUID> {
    
    // Find group members by group ID
    @Query("SELECT gm FROM GroupMember gm WHERE gm.group.id = :groupId AND gm.status = 'ACTIVE'")
    List<GroupMember> findActiveMembersByGroupId(@Param("groupId") UUID groupId);
    
    // Find group members by group ID with role
    @Query("SELECT gm FROM GroupMember gm WHERE gm.group.id = :groupId AND gm.role = :role AND gm.status = 'ACTIVE'")
    List<GroupMember> findActiveMembersByGroupIdAndRole(@Param("groupId") UUID groupId, @Param("role") GroupMember.MemberRole role);
    
    // Find group memberships by user ID
    @Query("SELECT gm FROM GroupMember gm WHERE gm.user.id = :userId AND gm.status = 'ACTIVE'")
    List<GroupMember> findActiveMembershipsByUserId(@Param("userId") UUID userId);
    
    // Find specific group membership
    @Query("SELECT gm FROM GroupMember gm WHERE gm.group.id = :groupId AND gm.user.id = :userId")
    Optional<GroupMember> findByGroupIdAndUserId(@Param("groupId") UUID groupId, @Param("userId") UUID userId);
    
    // Find group admins
    @Query("SELECT gm FROM GroupMember gm WHERE gm.group.id = :groupId AND gm.role = 'ADMIN' AND gm.status = 'ACTIVE'")
    List<GroupMember> findAdminsByGroupId(@Param("groupId") UUID groupId);
    
    // Find group moderators
    @Query("SELECT gm FROM GroupMember gm WHERE gm.group.id = :groupId AND (gm.role = 'ADMIN' OR gm.role = 'MODERATOR') AND gm.status = 'ACTIVE'")
    List<GroupMember> findModeratorsByGroupId(@Param("groupId") UUID groupId);
    
    // Count active members in group
    @Query("SELECT COUNT(gm) FROM GroupMember gm WHERE gm.group.id = :groupId AND gm.status = 'ACTIVE'")
    Long countActiveMembersByGroupId(@Param("groupId") UUID groupId);
    
    // Find pending memberships
    @Query("SELECT gm FROM GroupMember gm WHERE gm.group.id = :groupId AND gm.status = 'PENDING'")
    List<GroupMember> findPendingMembersByGroupId(@Param("groupId") UUID groupId);
    
    // Find members who left the group
    @Query("SELECT gm FROM GroupMember gm WHERE gm.group.id = :groupId AND gm.status = 'LEFT'")
    List<GroupMember> findLeftMembersByGroupId(@Param("groupId") UUID groupId);
    
    // Find members who were removed from the group
    @Query("SELECT gm FROM GroupMember gm WHERE gm.group.id = :groupId AND gm.status = 'REMOVED'")
    List<GroupMember> findRemovedMembersByGroupId(@Param("groupId") UUID groupId);
    
    // Check if user is member of group
    @Query("SELECT COUNT(gm) > 0 FROM GroupMember gm WHERE gm.group.id = :groupId AND gm.user.id = :userId AND gm.status = 'ACTIVE'")
    boolean isUserMemberOfGroup(@Param("groupId") UUID groupId, @Param("userId") UUID userId);
    
    // Check if user is admin of group
    @Query("SELECT COUNT(gm) > 0 FROM GroupMember gm WHERE gm.group.id = :groupId AND gm.user.id = :userId AND gm.role = 'ADMIN' AND gm.status = 'ACTIVE'")
    boolean isUserAdminOfGroup(@Param("groupId") UUID groupId, @Param("userId") UUID userId);
    
    // Check if user is moderator of group
    @Query("SELECT COUNT(gm) > 0 FROM GroupMember gm WHERE gm.group.id = :groupId AND gm.user.id = :userId AND (gm.role = 'ADMIN' OR gm.role = 'MODERATOR') AND gm.status = 'ACTIVE'")
    boolean isUserModeratorOfGroup(@Param("groupId") UUID groupId, @Param("userId") UUID userId);
    
    // Find muted members
    @Query("SELECT gm FROM GroupMember gm WHERE gm.group.id = :groupId AND gm.isMuted = true AND gm.status = 'ACTIVE'")
    List<GroupMember> findMutedMembersByGroupId(@Param("groupId") UUID groupId);
    
    // Find pinned members
    @Query("SELECT gm FROM GroupMember gm WHERE gm.group.id = :groupId AND gm.isPinned = true AND gm.status = 'ACTIVE'")
    List<GroupMember> findPinnedMembersByGroupId(@Param("groupId") UUID groupId);
    
    // Find group members by group ID and status
    @Query("SELECT gm FROM GroupMember gm WHERE gm.group.id = :groupId AND gm.status = :status")
    List<GroupMember> findByGroupIdAndStatus(@Param("groupId") UUID groupId, @Param("status") GroupMember.MemberStatus status);
}

