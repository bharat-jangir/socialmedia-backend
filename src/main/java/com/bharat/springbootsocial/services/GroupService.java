package com.bharat.springbootsocial.services;

import com.bharat.springbootsocial.entity.Group;
import com.bharat.springbootsocial.entity.GroupMember;
import com.bharat.springbootsocial.entity.User;
import com.bharat.springbootsocial.response.PaginatedResponse;

import java.util.List;
import java.util.UUID;

public interface GroupService {
    
    // Group CRUD operations
    Group createGroup(User creator, String name, String description, Group.GroupType groupType, Boolean isPublic, List<UUID> memberIds);
    Group updateGroup(UUID groupId, User user, String name, String description, String groupImage, String groupCoverImage);
    void deleteGroup(UUID groupId, User user);
    Group getGroupById(UUID groupId);
    List<Group> getGroupsByUserId(UUID userId);
    PaginatedResponse<Group> getGroupsByUserIdPaginated(UUID userId, int page, int size);
    
    // Group membership operations
    GroupMember joinGroup(UUID groupId, User user);
    void leaveGroup(UUID groupId, User user);
    GroupMember addMember(UUID groupId, User admin, UUID memberId);
    void removeMember(UUID groupId, User admin, UUID memberId);
    GroupMember updateMemberRole(UUID groupId, User admin, UUID memberId, GroupMember.MemberRole role);
    void muteMember(UUID groupId, User admin, UUID memberId, Boolean isMuted);
    void pinMember(UUID groupId, User admin, UUID memberId, Boolean isPinned);
    
    // Group member queries
    List<GroupMember> getGroupMembers(UUID groupId);
    List<GroupMember> getGroupAdmins(UUID groupId);
    List<GroupMember> getGroupModerators(UUID groupId);
    GroupMember getGroupMember(UUID groupId, UUID userId);
    boolean isUserMember(UUID groupId, UUID userId);
    boolean isUserAdmin(UUID groupId, UUID userId);
    boolean isUserModerator(UUID groupId, UUID userId);
    
    // Group discovery
    List<Group> getPublicGroups();
    PaginatedResponse<Group> getPublicGroupsPaginated(int page, int size);
    List<Group> searchGroups(String query);
    PaginatedResponse<Group> searchGroupsPaginated(String query, int page, int size);
    List<Group> getJoinableGroupsForUser(UUID userId);
    PaginatedResponse<Group> getJoinableGroupsForUserPaginated(UUID userId, int page, int size);
    
    // Group settings
    Group updateGroupSettings(UUID groupId, User admin, Boolean isPublic, Boolean allowMemberInvites, Integer maxMembers);
    Group updateGroupImage(UUID groupId, User admin, String imageUrl);
    Group updateGroupCoverImage(UUID groupId, User admin, String coverImageUrl);
    
    // Group statistics
    int getGroupMemberCount(UUID groupId);
    int getActiveGroupMemberCount(UUID groupId);
    Group.GroupStats getGroupStatistics(UUID groupId);
    
    // Group activity
    void updateGroupLastActivity(UUID groupId);
    List<Group> getGroupsWithRecentActivity(int hours);
    
    // Group validation
    boolean canUserJoinGroup(UUID groupId, UUID userId);
    boolean canUserLeaveGroup(UUID groupId, UUID userId);
    boolean canUserRemoveMember(UUID groupId, UUID adminId, UUID memberId);
    boolean canUserUpdateGroup(UUID groupId, UUID userId);
}

