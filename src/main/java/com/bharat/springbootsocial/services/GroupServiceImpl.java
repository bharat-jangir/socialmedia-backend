package com.bharat.springbootsocial.services;

import com.bharat.springbootsocial.entity.Group;
import com.bharat.springbootsocial.entity.GroupMember;
import com.bharat.springbootsocial.entity.User;
import com.bharat.springbootsocial.repository.GroupMemberRepo;
import com.bharat.springbootsocial.repository.GroupRepo;
import com.bharat.springbootsocial.repository.GroupMessageRepo;
import com.bharat.springbootsocial.repository.GroupCallRoomRepo;
import com.bharat.springbootsocial.response.PaginatedResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;
import java.util.Optional;

@Service
public class GroupServiceImpl implements GroupService {
    
    @Autowired
    private GroupRepo groupRepo;
    
    @Autowired
    private GroupMemberRepo groupMemberRepo;
    
    @Autowired
    private GroupMessageRepo groupMessageRepo;
    
    @Autowired
    private GroupCallRoomRepo groupCallRoomRepo;
    
    @Autowired
    private ServiceInt userService;
    
    @Autowired
    private NotificationService notificationService;
    
    @Override
    public Group createGroup(User creator, String name, String description, Group.GroupType groupType, Boolean isPublic, List<UUID> memberIds) {
        // Check if group name already exists
        if (groupRepo.findByName(name).isPresent()) {
            throw new IllegalArgumentException("Group name already exists");
        }
        
        Group group = new Group();
        group.setName(name);
        group.setDescription(description);
        group.setGroupType(groupType);
        group.setIsPublic(isPublic != null ? isPublic : false);
        group.setCreatedBy(creator);
        group.setAdmin(creator);
        group.setStatus(Group.GroupStatus.ACTIVE);
        
        Group savedGroup = groupRepo.save(group);
        
        // Add creator as admin
        GroupMember creatorMember = new GroupMember();
        creatorMember.setGroup(savedGroup);
        creatorMember.setUser(creator);
        creatorMember.setRole(GroupMember.MemberRole.ADMIN);
        creatorMember.setStatus(GroupMember.MemberStatus.ACTIVE);
        groupMemberRepo.save(creatorMember);
        
        // Add other members if provided
        if (memberIds != null && !memberIds.isEmpty()) {
            for (UUID memberId : memberIds) {
                if (!memberId.equals(creator.getId())) {
                    User member = userService.getUserById(memberId);
                    GroupMember groupMember = new GroupMember();
                    groupMember.setGroup(savedGroup);
                    groupMember.setUser(member);
                    groupMember.setRole(GroupMember.MemberRole.MEMBER);
                    groupMember.setStatus(GroupMember.MemberStatus.ACTIVE);
                    groupMemberRepo.save(groupMember);
                    
                    // Send notification
                    notificationService.sendGroupInvitationNotification(member, creator, savedGroup);
                }
            }
        }
        
        return savedGroup;
    }
    
    @Override
    public Group updateGroup(UUID groupId, User user, String name, String description, String groupImage, String groupCoverImage) {
        Group group = getGroupById(groupId);
        
        if (!canUserUpdateGroup(groupId, user.getId())) {
            throw new IllegalArgumentException("You are not authorized to update this group");
        }
        
        if (name != null && !name.trim().isEmpty()) {
            // Check if new name is different and doesn't already exist
            if (!group.getName().equals(name) && groupRepo.findByName(name).isPresent()) {
                throw new IllegalArgumentException("Group name already exists");
            }
            group.setName(name);
        }
        
        if (description != null) {
            group.setDescription(description);
        }
        
        if (groupImage != null) {
            group.setGroupImage(groupImage);
        }
        
        if (groupCoverImage != null) {
            group.setGroupCoverImage(groupCoverImage);
        }
        
        group.updateLastActivity();
        return groupRepo.save(group);
    }
    
    @Override
    public void deleteGroup(UUID groupId, User user) {
        Group group = getGroupById(groupId);
        
        if (!group.isCreator(user) && !group.isAdmin(user)) {
            throw new IllegalArgumentException("You are not authorized to delete this group");
        }
        
        // Hard delete - completely remove from database
        groupRepo.delete(group);
        
        // Note: This will cascade delete related entities if properly configured
        // Alternative: Soft delete (commented out below)
        // group.setStatus(Group.GroupStatus.DELETED);
        // groupRepo.save(group);
    }
    
    @Override
    public Group getGroupById(UUID groupId) {
        return groupRepo.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found with id: " + groupId));
    }
    
    @Override
    public List<Group> getGroupsByUserId(UUID userId) {
        return groupRepo.findGroupsByUserId(userId);
    }
    
    @Override
    public PaginatedResponse<Group> getGroupsByUserIdPaginated(UUID userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Group> groupsPage = groupRepo.findGroupsByUserIdPaginated(userId, pageable);
        
        return new PaginatedResponse<>(
                groupsPage.getContent(),
                groupsPage.getNumber(),
                groupsPage.getSize(),
                groupsPage.getTotalElements(),
                groupsPage.getTotalPages(),
                groupsPage.hasNext(),
                groupsPage.hasPrevious(),
                groupsPage.isFirst(),
                groupsPage.isLast()
        );
    }
    
    @Override
    public GroupMember joinGroup(UUID groupId, User user) {
        Group group = getGroupById(groupId);
        
        if (!group.canUserJoin(user)) {
            throw new IllegalArgumentException("You cannot join this group");
        }
        
        // Check if user is already a member
        Optional<GroupMember> existingMember = groupMemberRepo.findByGroupIdAndUserId(groupId, user.getId());
        if (existingMember.isPresent()) {
            GroupMember member = existingMember.get();
            if (member.getStatus() == GroupMember.MemberStatus.ACTIVE) {
                throw new IllegalArgumentException("You are already a member of this group");
            } else if (member.getStatus() == GroupMember.MemberStatus.LEFT) {
                // Rejoin the group
                member.setStatus(GroupMember.MemberStatus.ACTIVE);
                member.setJoinedAt(LocalDateTime.now());
                member.setLeftAt(null);
                group.updateLastActivity();
                groupRepo.save(group);
                return groupMemberRepo.save(member);
            }
        }
        
        // Add new member
        GroupMember groupMember = new GroupMember();
        groupMember.setGroup(group);
        groupMember.setUser(user);
        groupMember.setRole(GroupMember.MemberRole.MEMBER);
        groupMember.setStatus(GroupMember.MemberStatus.ACTIVE);
        
        group.updateLastActivity();
        groupRepo.save(group);
        
        return groupMemberRepo.save(groupMember);
    }
    
    @Override
    public void leaveGroup(UUID groupId, User user) {
        Group group = getGroupById(groupId);
        
        if (!group.isMember(user)) {
            throw new IllegalArgumentException("You are not a member of this group");
        }
        
        if (group.isCreator(user)) {
            throw new IllegalArgumentException("Group creator cannot leave the group. Transfer ownership or delete the group.");
        }
        
        GroupMember member = groupMemberRepo.findByGroupIdAndUserId(groupId, user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));
        
        member.leaveGroup();
        groupMemberRepo.save(member);
        
        group.updateLastActivity();
        groupRepo.save(group);
    }
    
    @Override
    public GroupMember addMember(UUID groupId, User admin, UUID memberId) {
        Group group = getGroupById(groupId);
        
        if (!canUserUpdateGroup(groupId, admin.getId())) {
            throw new IllegalArgumentException("You are not authorized to add members to this group");
        }
        
        User member = userService.getUserById(memberId);
        
        if (group.isMember(member)) {
            throw new IllegalArgumentException("User is already a member of this group");
        }
        
        if (group.getActiveMemberCount() >= group.getMaxMembers()) {
            throw new IllegalArgumentException("Group has reached maximum member limit");
        }
        
        GroupMember groupMember = new GroupMember();
        groupMember.setGroup(group);
        groupMember.setUser(member);
        groupMember.setRole(GroupMember.MemberRole.MEMBER);
        groupMember.setStatus(GroupMember.MemberStatus.ACTIVE);
        
        group.updateLastActivity();
        groupRepo.save(group);
        
        // Send notification
        notificationService.sendGroupInvitationNotification(member, admin, group);
        
        return groupMemberRepo.save(groupMember);
    }
    
    @Override
    public void removeMember(UUID groupId, User admin, UUID memberId) {
        Group group = getGroupById(groupId);
        
        if (!canUserRemoveMember(groupId, admin.getId(), memberId)) {
            throw new IllegalArgumentException("You are not authorized to remove this member");
        }
        
        GroupMember member = groupMemberRepo.findByGroupIdAndUserId(groupId, memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));
        
        member.removeFromGroup();
        groupMemberRepo.save(member);
        
        group.updateLastActivity();
        groupRepo.save(group);
    }
    
    @Override
    public GroupMember updateMemberRole(UUID groupId, User admin, UUID memberId, GroupMember.MemberRole role) {
        Group group = getGroupById(groupId);
        
        if (!group.isAdmin(admin)) {
            throw new IllegalArgumentException("Only group admins can update member roles");
        }
        
        GroupMember member = groupMemberRepo.findByGroupIdAndUserId(groupId, memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));
        
        if (member.getUser().getId().equals(admin.getId())) {
            throw new IllegalArgumentException("You cannot change your own role");
        }
        
        member.setRole(role);
        return groupMemberRepo.save(member);
    }
    
    @Override
    public void muteMember(UUID groupId, User admin, UUID memberId, Boolean isMuted) {
        if (!canUserUpdateGroup(groupId, admin.getId())) {
            throw new IllegalArgumentException("You are not authorized to mute members");
        }
        
        GroupMember member = groupMemberRepo.findByGroupIdAndUserId(groupId, memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));
        
        member.setIsMuted(isMuted);
        groupMemberRepo.save(member);
    }
    
    @Override
    public void pinMember(UUID groupId, User admin, UUID memberId, Boolean isPinned) {
        if (!canUserUpdateGroup(groupId, admin.getId())) {
            throw new IllegalArgumentException("You are not authorized to pin members");
        }
        
        GroupMember member = groupMemberRepo.findByGroupIdAndUserId(groupId, memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));
        
        member.setIsPinned(isPinned);
        groupMemberRepo.save(member);
    }
    
    @Override
    public List<GroupMember> getGroupMembers(UUID groupId) {
        return groupMemberRepo.findActiveMembersByGroupId(groupId);
    }
    
    @Override
    public List<GroupMember> getGroupAdmins(UUID groupId) {
        return groupMemberRepo.findAdminsByGroupId(groupId);
    }
    
    @Override
    public List<GroupMember> getGroupModerators(UUID groupId) {
        return groupMemberRepo.findModeratorsByGroupId(groupId);
    }
    
    @Override
    public GroupMember getGroupMember(UUID groupId, UUID userId) {
        return groupMemberRepo.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));
    }
    
    @Override
    public boolean isUserMember(UUID groupId, UUID userId) {
        return groupMemberRepo.isUserMemberOfGroup(groupId, userId);
    }
    
    @Override
    public boolean isUserAdmin(UUID groupId, UUID userId) {
        return groupMemberRepo.isUserAdminOfGroup(groupId, userId);
    }
    
    @Override
    public boolean isUserModerator(UUID groupId, UUID userId) {
        return groupMemberRepo.isUserModeratorOfGroup(groupId, userId);
    }
    
    @Override
    public List<Group> getPublicGroups() {
        return groupRepo.findPublicGroups();
    }
    
    @Override
    public PaginatedResponse<Group> getPublicGroupsPaginated(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Group> groupsPage = groupRepo.findPublicGroupsPaginated(pageable);
        
        return new PaginatedResponse<>(
                groupsPage.getContent(),
                groupsPage.getNumber(),
                groupsPage.getSize(),
                groupsPage.getTotalElements(),
                groupsPage.getTotalPages(),
                groupsPage.hasNext(),
                groupsPage.hasPrevious(),
                groupsPage.isFirst(),
                groupsPage.isLast()
        );
    }
    
    @Override
    public List<Group> searchGroups(String query) {
        return groupRepo.searchGroupsByName(query);
    }
    
    @Override
    public PaginatedResponse<Group> searchGroupsPaginated(String query, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Group> groupsPage = groupRepo.searchGroupsByNamePaginated(query, pageable);
        
        return new PaginatedResponse<>(
                groupsPage.getContent(),
                groupsPage.getNumber(),
                groupsPage.getSize(),
                groupsPage.getTotalElements(),
                groupsPage.getTotalPages(),
                groupsPage.hasNext(),
                groupsPage.hasPrevious(),
                groupsPage.isFirst(),
                groupsPage.isLast()
        );
    }
    
    @Override
    public List<Group> getJoinableGroupsForUser(UUID userId) {
        return groupRepo.findJoinableGroupsForUser(userId);
    }
    
    @Override
    public PaginatedResponse<Group> getJoinableGroupsForUserPaginated(UUID userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Group> groupsPage = groupRepo.findJoinableGroupsForUserPaginated(userId, pageable);
        
        return new PaginatedResponse<>(
                groupsPage.getContent(),
                groupsPage.getNumber(),
                groupsPage.getSize(),
                groupsPage.getTotalElements(),
                groupsPage.getTotalPages(),
                groupsPage.hasNext(),
                groupsPage.hasPrevious(),
                groupsPage.isFirst(),
                groupsPage.isLast()
        );
    }
    
    @Override
    public Group updateGroupSettings(UUID groupId, User admin, Boolean isPublic, Boolean allowMemberInvites, Integer maxMembers) {
        Group group = getGroupById(groupId);
        
        if (!group.isAdmin(admin)) {
            throw new IllegalArgumentException("Only group admins can update group settings");
        }
        
        if (isPublic != null) {
            group.setIsPublic(isPublic);
        }
        
        if (allowMemberInvites != null) {
            group.setAllowMemberInvites(allowMemberInvites);
        }
        
        if (maxMembers != null && maxMembers > 0) {
            if (maxMembers < group.getActiveMemberCount()) {
                throw new IllegalArgumentException("Max members cannot be less than current active members");
            }
            group.setMaxMembers(maxMembers);
        }
        
        group.updateLastActivity();
        return groupRepo.save(group);
    }
    
    @Override
    public Group updateGroupImage(UUID groupId, User admin, String imageUrl) {
        Group group = getGroupById(groupId);
        
        if (!canUserUpdateGroup(groupId, admin.getId())) {
            throw new IllegalArgumentException("You are not authorized to update group image");
        }
        
        group.setGroupImage(imageUrl);
        group.updateLastActivity();
        return groupRepo.save(group);
    }
    
    @Override
    public Group updateGroupCoverImage(UUID groupId, User admin, String coverImageUrl) {
        Group group = getGroupById(groupId);
        
        if (!canUserUpdateGroup(groupId, admin.getId())) {
            throw new IllegalArgumentException("You are not authorized to update group cover image");
        }
        
        group.setGroupCoverImage(coverImageUrl);
        group.updateLastActivity();
        return groupRepo.save(group);
    }
    
    @Override
    public int getGroupMemberCount(UUID groupId) {
        return groupMemberRepo.countActiveMembersByGroupId(groupId).intValue();
    }
    
    @Override
    public int getActiveGroupMemberCount(UUID groupId) {
        return getGroupMemberCount(groupId);
    }
    
    @Override
    public Group.GroupStats getGroupStatistics(UUID groupId) {
        Group group = getGroupById(groupId);
        
        int totalMembers = getGroupMemberCount(groupId);
        int totalMessages = groupMessageRepo.countMessagesByGroupId(groupId).intValue();
        int totalCallRooms = (int) groupCallRoomRepo.countByGroup(group);
        int activeCallRooms = (int) groupCallRoomRepo.countByGroupIdAndIsActiveTrue(groupId);
        
        return new Group.GroupStats(
                group.getId(),
                group.getName(),
                totalMembers,
                totalMembers, // Active members same as total for now
                totalMessages,
                totalCallRooms,
                activeCallRooms,
                group.getLastActivity(),
                group.getStatus()
        );
    }
    
    @Override
    public void updateGroupLastActivity(UUID groupId) {
        Group group = getGroupById(groupId);
        group.updateLastActivity();
        groupRepo.save(group);
    }
    
    @Override
    public List<Group> getGroupsWithRecentActivity(int hours) {
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        return groupRepo.findGroupsWithRecentActivity(since);
    }
    
    @Override
    public boolean canUserJoinGroup(UUID groupId, UUID userId) {
        Group group = getGroupById(groupId);
        User user = userService.getUserById(userId);
        return group.canUserJoin(user);
    }
    
    @Override
    public boolean canUserLeaveGroup(UUID groupId, UUID userId) {
        Group group = getGroupById(groupId);
        User user = userService.getUserById(userId);
        return group.isMember(user) && !group.isCreator(user);
    }
    
    @Override
    public boolean canUserRemoveMember(UUID groupId, UUID adminId, UUID memberId) {
        Group group = getGroupById(groupId);
        User admin = userService.getUserById(adminId);
        User member = userService.getUserById(memberId);
        
        if (!group.isAdmin(admin)) {
            return false;
        }
        
        if (group.isCreator(member)) {
            return false; // Cannot remove group creator
        }
        
        if (adminId.equals(memberId)) {
            return false; // Cannot remove self
        }
        
        return true;
    }
    
    @Override
    public boolean canUserUpdateGroup(UUID groupId, UUID userId) {
        Group group = getGroupById(groupId);
        User user = userService.getUserById(userId);
        return group.isAdmin(user) || group.isCreator(user);
    }
}
