package com.bharat.springbootsocial.services;

import com.bharat.springbootsocial.entity.Group;
import com.bharat.springbootsocial.entity.GroupCallRoom;
import com.bharat.springbootsocial.entity.GroupCallSession;
import com.bharat.springbootsocial.entity.GroupMember;
import com.bharat.springbootsocial.entity.User;
import com.bharat.springbootsocial.exception.UserException;
import com.bharat.springbootsocial.repository.GroupCallRoomRepo;
import com.bharat.springbootsocial.repository.GroupCallSessionRepo;
import com.bharat.springbootsocial.repository.GroupMemberRepo;
import com.bharat.springbootsocial.repository.GroupRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.UUID;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GroupCallServiceImpl implements GroupCallService {
    
    @Autowired
    private GroupCallRoomRepo groupCallRoomRepo;
    
    @Autowired
    private GroupRepo groupRepo;
    
    @Autowired
    private GroupMemberRepo groupMemberRepo;
    
    @Autowired
    private GroupCallSessionRepo groupCallSessionRepo;
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    @Override
    public GroupCallRoom createGroupCallRoom(User creator, UUID groupId, GroupCallRoom.CallType callType, String roomName) throws UserException {
        try {
            System.out.println("=== DEBUG: Creating group call room ===");
            System.out.println("Creator ID: " + creator.getId() + " (" + creator.getFname() + " " + creator.getLname() + ")");
            System.out.println("Group ID: " + groupId);
            System.out.println("Call Type: " + callType);
            System.out.println("Room Name: " + roomName);
            
            // Validate group exists
            Group group = groupRepo.findById(groupId)
                    .orElseThrow(() -> new UserException("Group not found with id: " + groupId));
            
            // Check if user is member of the group
            if (!isUserGroupMember(groupId, creator.getId())) {
                throw new UserException("User is not a member of this group");
            }
            
            // Check if user can create group call room
            if (!canUserCreateGroupCallRoom(groupId, creator)) {
                throw new UserException("User cannot create group call room");
            }
            
            // Create new group call room
            GroupCallRoom room = new GroupCallRoom();
            room.setRoomName(roomName != null ? roomName : "Group Call - " + group.getName());
            room.setGroup(group);
            room.setCreatedBy(creator);
            room.setCallType(callType);
            room.setStatus(GroupCallRoom.CallStatus.WAITING);
            room.setIsActive(true);
            room.setMaxParticipants(50); // Default for group calls
            
            // Add creator as first participant
            room.addParticipant(creator);
            
            GroupCallRoom savedRoom = groupCallRoomRepo.save(room);
            System.out.println("Group call room saved successfully with ID: " + savedRoom.getId() + ", Room ID: " + savedRoom.getRoomId());
            
            // Notify group members about the new call room
            System.out.println("🔔 About to call notifyGroupCallRoomCreated...");
            notifyGroupCallRoomCreated(savedRoom);
            System.out.println("🔔 notifyGroupCallRoomCreated call completed.");
            
            return savedRoom;
            
        } catch (Exception e) {
            System.err.println("Failed to create group call room: " + e.getMessage());
            throw new UserException("Failed to create group call room: " + e.getMessage());
        }
    }
    
    @Override
    public GroupCallRoom joinGroupCallRoom(String roomId, User user) throws UserException {
        try {
            GroupCallRoom room = getGroupCallRoomById(roomId);
            
            // Check if user can join
            if (!canUserJoinGroupCallRoom(roomId, user)) {
                throw new UserException("User cannot join this group call room");
            }
            
            // Add user as participant if not already
            if (!room.isParticipant(user)) {
                room.addParticipant(user);
                groupCallRoomRepo.save(room);
                
                // Create or update call session
                createOrUpdateGroupCallSession(room, user);
                
                // Notify other participants
                notifyUserJoinedGroupCallRoom(room, user);
            }
            
            return room;
            
        } catch (Exception e) {
            throw new UserException("Failed to join group call room: " + e.getMessage());
        }
    }
    
    @Override
    public void leaveGroupCallRoom(String roomId, User user) throws UserException {
        try {
            GroupCallRoom room = getGroupCallRoomById(roomId);
            
            if (room.isParticipant(user)) {
                room.removeParticipant(user);
                groupCallRoomRepo.save(room);
                
                // Update call session
                updateGroupCallSessionOnLeave(room, user);
                
                // Notify other participants
                notifyUserLeftGroupCallRoom(room, user);
                
                // If no participants left, end the call
                if (room.getParticipants().isEmpty()) {
                    endGroupCallRoom(roomId, user);
                }
            }
            
        } catch (Exception e) {
            throw new UserException("Failed to leave group call room: " + e.getMessage());
        }
    }
    
    @Override
    public void endGroupCallRoom(String roomId, User user) throws UserException {
        try {
            GroupCallRoom room = getGroupCallRoomById(roomId);
            
            // Check if user can end the call
            if (!canUserEndGroupCallRoom(roomId, user)) {
                throw new UserException("User cannot end this group call room");
            }
            
            room.endCall();
            groupCallRoomRepo.save(room);
            
            // Notify all participants
            notifyGroupCallRoomEnded(room);
            
        } catch (Exception e) {
            throw new UserException("Failed to end group call room: " + e.getMessage());
        }
    }
    
    @Override
    public GroupCallRoom getGroupCallRoomById(String roomId) throws UserException {
        return groupCallRoomRepo.findByRoomId(roomId)
                .orElseThrow(() -> new UserException("Group call room not found with id: " + roomId));
    }
    
    @Override
    public List<GroupCallRoom> getActiveGroupCallRooms(UUID groupId) {
        try {
            System.out.println("🔍 Getting active group call rooms for group ID: " + groupId);
            Group group = groupRepo.findById(groupId)
                    .orElseThrow(() -> new UserException("Group not found with id: " + groupId));
            System.out.println("Group found: " + group.getName() + " (ID: " + group.getId() + ")");
            
            List<GroupCallRoom> activeRooms = groupCallRoomRepo.findByGroupAndIsActiveTrue(group);
            System.out.println("Active rooms found: " + activeRooms.size());
            
            for (GroupCallRoom room : activeRooms) {
                System.out.println("  - Room: " + room.getRoomId() + ", Status: " + room.getStatus() + ", Active: " + room.getIsActive());
            }
            
            return activeRooms;
        } catch (UserException e) {
            System.err.println("Error getting active group call rooms: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    @Override
    public List<GroupCallRoom> getGroupCallRoomsByGroup(UUID groupId) {
        try {
            Group group = groupRepo.findById(groupId)
                    .orElseThrow(() -> new UserException("Group not found with id: " + groupId));
            return groupCallRoomRepo.findByGroup(group);
        } catch (UserException e) {
            System.err.println("Error getting group call rooms: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    @Override
    public List<GroupCallRoom> getGroupCallRoomsByUser(User user) {
        return groupCallRoomRepo.findByParticipantsContaining(user);
    }
    
    @Override
    public List<GroupCallRoom> getGroupCallRoomsByStatus(GroupCallRoom.CallStatus status) {
        return groupCallRoomRepo.findByStatus(status);
    }
    
    
    @Override
    public void addParticipantToGroupCallRoom(String roomId, User user) throws UserException {
        GroupCallRoom room = getGroupCallRoomById(roomId);
        
        if (!room.isParticipant(user)) {
            room.addParticipant(user);
            groupCallRoomRepo.save(room);
            notifyUserJoinedGroupCallRoom(room, user);
        }
    }
    
    @Override
    public void removeParticipantFromGroupCallRoom(String roomId, User user) throws UserException {
        GroupCallRoom room = getGroupCallRoomById(roomId);
        
        if (room.isParticipant(user)) {
            room.removeParticipant(user);
            groupCallRoomRepo.save(room);
            notifyUserLeftGroupCallRoom(room, user);
        }
    }
    
    @Override
    public boolean canUserJoinGroupCallRoom(String roomId, User user) throws UserException {
        GroupCallRoom room = getGroupCallRoomById(roomId);
        
        // Check if room is active and can accept participants
        if (!room.canJoin()) {
            return false;
        }
        
        // Check if user is member of the group
        return isUserGroupMember(room.getGroup().getId(), user.getId());
    }
    
    @Override
    public boolean canUserCreateGroupCallRoom(UUID groupId, User user) throws UserException {
        System.out.println("=== 🔍 CHECKING IF USER CAN CREATE GROUP CALL ROOM ===");
        System.out.println("Group ID: " + groupId);
        System.out.println("User ID: " + user.getId() + " (" + user.getFname() + " " + user.getLname() + ")");
        
        // Check if user is member of the group
        boolean isMember = isUserGroupMember(groupId, user.getId());
        System.out.println("Is user group member: " + isMember);
        
        if (!isMember) {
            System.out.println("❌ User is not a member of the group");
            return false;
        }
        
        // Check if there's already an active call in the group
        System.out.println("🔍 Checking for active rooms in group...");
        List<GroupCallRoom> activeRooms = getActiveGroupCallRooms(groupId);
        System.out.println("Active rooms found: " + activeRooms.size());
        
        for (GroupCallRoom room : activeRooms) {
            System.out.println("  - Room ID: " + room.getRoomId() + ", Status: " + room.getStatus() + ", Active: " + room.getIsActive());
        }
        
        // Allow multiple calls in a group (remove the restriction)
        boolean canCreate = true; // Always allow creating new calls
        System.out.println("Can create group call room: " + canCreate + " (Multiple calls allowed)");
        System.out.println("=== ✅ USER CAN CREATE CHECK COMPLETED ===");
        
        return canCreate;
    }
    
    @Override
    public boolean canUserEndGroupCallRoom(String roomId, User user) throws UserException {
        GroupCallRoom room = getGroupCallRoomById(roomId);
        
        // Creator can always end the call
        if (room.getCreatedBy().getId().equals(user.getId())) {
            return true;
        }
        
        // Group admins can end the call
        return isUserGroupAdmin(room.getGroup().getId(), user.getId());
    }
    
    @Override
    public GroupCallRoom startGroupCall(String roomId, User user) throws UserException {
        GroupCallRoom room = getGroupCallRoomById(roomId);
        
        if (!canUserEndGroupCallRoom(roomId, user)) {
            throw new UserException("User cannot start this group call");
        }
        
        room.startCall();
        GroupCallRoom savedRoom = groupCallRoomRepo.save(room);
        
        notifyGroupCallRoomStarted(savedRoom);
        
        return savedRoom;
    }
    
    @Override
    public GroupCallRoom pauseGroupCall(String roomId, User user) throws UserException {
        // Implementation for pausing group call
        throw new UserException("Pause functionality not implemented yet");
    }
    
    @Override
    public GroupCallRoom resumeGroupCall(String roomId, User user) throws UserException {
        // Implementation for resuming group call
        throw new UserException("Resume functionality not implemented yet");
    }
    
    @Override
    public int getGroupCallRoomParticipantCount(String roomId) throws UserException {
        GroupCallRoom room = getGroupCallRoomById(roomId);
        return room.getParticipants().size();
    }
    
    @Override
    public long getGroupCallRoomDuration(String roomId) throws UserException {
        GroupCallRoom room = getGroupCallRoomById(roomId);
        return room.getDurationSeconds() != null ? room.getDurationSeconds() : 0L;
    }
    
    @Override
    public boolean isGroupCallRoomActive(String roomId) throws UserException {
        GroupCallRoom room = getGroupCallRoomById(roomId);
        return room.getIsActive() && room.getStatus() == GroupCallRoom.CallStatus.ACTIVE;
    }
    
    // Notification methods
    @Override
    public void notifyGroupCallRoomCreated(GroupCallRoom room) {
        try {
            System.out.println("=== 📢 NOTIFYING GROUP MEMBERS ABOUT NEW CALL ===");
            System.out.println("Room ID: " + room.getRoomId());
            System.out.println("Group ID: " + room.getGroup().getId());
            System.out.println("Created By: " + room.getCreatedBy().getFname() + " " + room.getCreatedBy().getLname());
            System.out.println("Created By ID: " + room.getCreatedBy().getId());
            
            // Send notification to all group members
            System.out.println("🔍 Fetching group members for Group ID: " + room.getGroup().getId());
            List<GroupMember> groupMembers = groupMemberRepo.findActiveMembersByGroupId(room.getGroup().getId());
            System.out.println("Total group members found: " + groupMembers.size());
            
            if (groupMembers.isEmpty()) {
                System.out.println("⚠️ No group members found! This might be the issue.");
                return;
            }
            
            for (GroupMember member : groupMembers) {
                System.out.println("🔍 Processing member: " + member.getUser().getFname() + " " + member.getUser().getLname() + " (ID: " + member.getUser().getId() + ")");
                System.out.println("🔍 Creator ID: " + room.getCreatedBy().getId() + ", Member ID: " + member.getUser().getId());
                
                if (!member.getUser().getId().equals(room.getCreatedBy().getId())) {
                    System.out.println("✅ Sending notification to user: " + member.getUser().getFname() + " " + member.getUser().getLname() + " (ID: " + member.getUser().getId() + ")");
                    
                    // Send to call-invitations queue (same as one-to-one calls)
                    String destination = "/user/" + member.getUser().getId() + "/queue/call-invitations";
                    
                    // Create invitation data
                    Map<String, Object> invitationData = new HashMap<>();
                    invitationData.put("groupId", room.getGroup().getId());
                    invitationData.put("roomId", room.getRoomId());
                    invitationData.put("roomName", room.getRoomName());
                    invitationData.put("callType", room.getCallType().toString());
                    invitationData.put("from", room.getCreatedBy().getId());
                    invitationData.put("fromName", room.getCreatedBy().getFname() + " " + room.getCreatedBy().getLname());
                    invitationData.put("fromProfileImage", room.getCreatedBy().getProfileImage() != null ? room.getCreatedBy().getProfileImage() : "");
                    invitationData.put("messageType", "GROUP_CALL_INVITATION");
                    
                    // Create invitation message
                    Map<String, Object> invitationMessage = new HashMap<>();
                    invitationMessage.put("type", "GROUP_CALL_INVITATION");
                    invitationMessage.put("relatedEntityType", "GROUP_CALL");
                    invitationMessage.put("data", invitationData);
                    invitationMessage.put("timestamp", System.currentTimeMillis());
                    
                    messagingTemplate.convertAndSend(destination, invitationMessage);
                    System.out.println("✅ Group call invitation sent to: " + destination);
                    
                    // Also send to group messaging for visibility
                    String groupDestination = "/group/" + room.getGroup().getId();
                    Map<String, Object> groupNotification = new HashMap<>();
                    groupNotification.put("type", "group-call-notification");
                    groupNotification.put("roomId", room.getRoomId());
                    groupNotification.put("roomName", room.getRoomName());
                    groupNotification.put("callType", room.getCallType());
                    groupNotification.put("createdBy", room.getCreatedBy().getFname() + " " + room.getCreatedBy().getLname());
                    groupNotification.put("createdById", room.getCreatedBy().getId());
                    groupNotification.put("groupId", room.getGroup().getId());
                    groupNotification.put("timestamp", System.currentTimeMillis());
                    groupNotification.put("message", room.getCreatedBy().getFname() + " started a " + room.getCallType() + " call");
                    
                    messagingTemplate.convertAndSend(groupDestination, groupNotification);
                    System.out.println("✅ Group notification sent to: " + groupDestination);
                } else {
                    System.out.println("⏭️ Skipping creator: " + member.getUser().getFname() + " " + member.getUser().getLname() + " (ID: " + member.getUser().getId() + ")");
                }
            }
            
            System.out.println("=== ✅ GROUP CALL NOTIFICATIONS SENT ===");
            
        } catch (Exception e) {
            System.err.println("Failed to send group call room created notification: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Method to send group call invitation (for testing)
    public void sendGroupCallInvitation(GroupCallRoom room, User toUser) {
        try {
            System.out.println("=== 📞 SENDING GROUP CALL INVITATION ===");
            System.out.println("Room ID: " + room.getRoomId());
            System.out.println("Group ID: " + room.getGroup().getId());
            System.out.println("To User: " + toUser.getFname() + " " + toUser.getLname() + " (ID: " + toUser.getId() + ")");
            
            // Send to call-invitations queue
            String destination = "/user/" + toUser.getId() + "/queue/call-invitations";
            
            // Create invitation data
            Map<String, Object> invitationData = new HashMap<>();
            invitationData.put("groupId", room.getGroup().getId());
            invitationData.put("roomId", room.getRoomId());
            invitationData.put("roomName", room.getRoomName());
            invitationData.put("callType", room.getCallType().toString());
            invitationData.put("from", room.getCreatedBy().getId());
            invitationData.put("fromName", room.getCreatedBy().getFname() + " " + room.getCreatedBy().getLname());
            invitationData.put("fromProfileImage", room.getCreatedBy().getProfileImage() != null ? room.getCreatedBy().getProfileImage() : "");
            invitationData.put("messageType", "GROUP_CALL_INVITATION");
            
            // Create invitation message
            Map<String, Object> invitationMessage = new HashMap<>();
            invitationMessage.put("type", "GROUP_CALL_INVITATION");
            invitationMessage.put("relatedEntityType", "GROUP_CALL");
            invitationMessage.put("data", invitationData);
            invitationMessage.put("timestamp", System.currentTimeMillis());
            
            messagingTemplate.convertAndSend(destination, invitationMessage);
            System.out.println("✅ Group call invitation sent to: " + destination);
            System.out.println("📨 Invitation message: " + invitationMessage);
            System.out.println("=== ✅ GROUP CALL INVITATION SENT ===");
            
        } catch (Exception e) {
            System.err.println("Failed to send group call invitation: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Override
    public void notifyGroupCallRoomStarted(GroupCallRoom room) {
        // Implementation for notifying call started
    }
    
    @Override
    public void notifyGroupCallRoomEnded(GroupCallRoom room) {
        // Implementation for notifying call ended
    }
    
    @Override
    public void notifyUserJoinedGroupCallRoom(GroupCallRoom room, User user) {
        // Implementation for notifying user joined
    }
    
    @Override
    public void notifyUserLeftGroupCallRoom(GroupCallRoom room, User user) {
        // Implementation for notifying user left
    }
    
    // Helper methods
    private boolean isUserGroupMember(UUID groupId, UUID userId) {
        System.out.println("🔍 Checking if user " + userId + " is member of group " + groupId);
        boolean isMember = groupMemberRepo.isUserMemberOfGroup(groupId, userId);
        System.out.println("User " + userId + " is member of group " + groupId + ": " + isMember);
        return isMember;
    }
    
    private boolean isUserGroupAdmin(UUID groupId, UUID userId) {
        return groupMemberRepo.isUserAdminOfGroup(groupId, userId);
    }
    
    @Override
    public List<User> getRoomParticipants(String roomId) throws UserException {
        GroupCallRoom room = getGroupCallRoomById(roomId);
        return new ArrayList<>(room.getParticipants());
    }
    
    // ==================== GROUP CALL SESSION MANAGEMENT ====================
    
    /**
     * Create or update group call session for a user
     */
    private void createOrUpdateGroupCallSession(GroupCallRoom room, User user) {
        try {
            System.out.println("=== 🔗 CREATING/UPDATING GROUP CALL SESSION ===");
            System.out.println("Room ID: " + room.getId() + ", Room String ID: " + room.getRoomId());
            System.out.println("User ID: " + user.getId());
            
            // Try to find existing session
            java.util.Optional<GroupCallSession> sessionOpt = groupCallSessionRepo.findByGroupCallRoomAndUser(room, user);
            
            if (sessionOpt.isPresent()) {
                // Update existing session
                GroupCallSession session = sessionOpt.get();
                session.setStatus(GroupCallSession.SessionStatus.JOINING);
                session.updateActivity();
                groupCallSessionRepo.save(session);
                System.out.println("Updated existing group call session for user " + user.getId() + " in room " + room.getRoomId());
            } else {
                // Create new session
                createGroupCallSession(room, user);
            }
            
        } catch (Exception e) {
            System.err.println("Failed to create/update group call session: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Create new group call session
     */
    private void createGroupCallSession(GroupCallRoom room, User user) {
        try {
            GroupCallSession session = new GroupCallSession();
            session.setGroupCallRoom(room);
            session.setUser(user);
            session.setStatus(GroupCallSession.SessionStatus.JOINING);
            session.setIsMuted(false);
            session.setIsVideoEnabled(true);
            session.setIsSpeaking(false);
            session.setConnectionState("new");
            session.setIceConnectionState("new");
            
            GroupCallSession savedSession = groupCallSessionRepo.save(session);
            System.out.println("Created new group call session for user " + user.getId() + " in room " + room.getRoomId() + " with session ID: " + savedSession.getSessionId());
            
        } catch (Exception e) {
            System.err.println("Failed to create group call session: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Update group call session when user leaves
     */
    private void updateGroupCallSessionOnLeave(GroupCallRoom room, User user) {
        try {
            java.util.Optional<GroupCallSession> sessionOpt = groupCallSessionRepo.findByGroupCallRoomAndUser(room, user);
            
            if (sessionOpt.isPresent()) {
                GroupCallSession session = sessionOpt.get();
                session.leaveSession();
                groupCallSessionRepo.save(session);
                System.out.println("Updated group call session for user " + user.getId() + " - marked as left");
            }
            
        } catch (Exception e) {
            System.err.println("Failed to update group call session on leave: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Get active sessions for a group call room
     */
    public List<GroupCallSession> getActiveGroupCallSessions(String roomId) throws UserException {
        GroupCallRoom room = getGroupCallRoomById(roomId);
        return groupCallSessionRepo.findActiveSessionsByRoom(room);
    }
    
    /**
     * Update group call session connection state
     */
    public void updateGroupCallSessionState(String roomId, User user, String connectionState, String iceConnectionState) throws UserException {
        try {
            GroupCallRoom room = getGroupCallRoomById(roomId);
            java.util.Optional<GroupCallSession> sessionOpt = groupCallSessionRepo.findByGroupCallRoomAndUser(room, user);
            
            if (sessionOpt.isPresent()) {
                GroupCallSession session = sessionOpt.get();
                session.setConnectionState(connectionState);
                session.setIceConnectionState(iceConnectionState);
                session.updateActivity();
                groupCallSessionRepo.save(session);
                
                System.out.println("Group call session state updated for user " + user.getId() + " in room " + roomId);
            }
            
        } catch (Exception e) {
            throw new UserException("Failed to update group call session state: " + e.getMessage());
        }
    }
}
