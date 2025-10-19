package com.bharat.springbootsocial.services;

import com.bharat.springbootsocial.entity.GroupCallRoom;
import com.bharat.springbootsocial.entity.GroupCallSession;
import com.bharat.springbootsocial.entity.User;
import java.util.UUID;
import com.bharat.springbootsocial.exception.UserException;

import java.util.List;

public interface GroupCallService {
    
    // Group call room management
    GroupCallRoom createGroupCallRoom(User creator, UUID groupId, GroupCallRoom.CallType callType, String roomName) throws UserException;
    GroupCallRoom joinGroupCallRoom(String roomId, User user) throws UserException;
    void leaveGroupCallRoom(String roomId, User user) throws UserException;
    void endGroupCallRoom(String roomId, User user) throws UserException;
    GroupCallRoom getGroupCallRoomById(String roomId) throws UserException;
    
    // Group call room queries
    List<GroupCallRoom> getActiveGroupCallRooms(UUID groupId);
    List<GroupCallRoom> getGroupCallRoomsByGroup(UUID groupId);
    List<GroupCallRoom> getGroupCallRoomsByUser(User user);
    List<GroupCallRoom> getGroupCallRoomsByStatus(GroupCallRoom.CallStatus status);
    
    // Group call room participants
    void addParticipantToGroupCallRoom(String roomId, User user) throws UserException;
    void removeParticipantFromGroupCallRoom(String roomId, User user) throws UserException;
    
    // Group call room validation
    boolean canUserJoinGroupCallRoom(String roomId, User user) throws UserException;
    boolean canUserCreateGroupCallRoom(UUID groupId, User user) throws UserException;
    boolean canUserEndGroupCallRoom(String roomId, User user) throws UserException;
    
    // Group call room operations
    GroupCallRoom startGroupCall(String roomId, User user) throws UserException;
    GroupCallRoom pauseGroupCall(String roomId, User user) throws UserException;
    GroupCallRoom resumeGroupCall(String roomId, User user) throws UserException;
    
    // Group call room statistics
    int getGroupCallRoomParticipantCount(String roomId) throws UserException;
    long getGroupCallRoomDuration(String roomId) throws UserException;
    boolean isGroupCallRoomActive(String roomId) throws UserException;
    
    // Group call room notifications
    void notifyGroupCallRoomCreated(GroupCallRoom room);
    void notifyGroupCallRoomStarted(GroupCallRoom room);
    void notifyGroupCallRoomEnded(GroupCallRoom room);
    void notifyUserJoinedGroupCallRoom(GroupCallRoom room, User user);
    void notifyUserLeftGroupCallRoom(GroupCallRoom room, User user);
    
    List<User> getRoomParticipants(String roomId) throws UserException;
    
    // Group call session management
    List<GroupCallSession> getActiveGroupCallSessions(String roomId) throws UserException;
    void updateGroupCallSessionState(String roomId, User user, String connectionState, String iceConnectionState) throws UserException;
}
