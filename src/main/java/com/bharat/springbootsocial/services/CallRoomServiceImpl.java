package com.bharat.springbootsocial.services;

import com.bharat.springbootsocial.entity.CallRoom;
import com.bharat.springbootsocial.entity.CallSession;
import com.bharat.springbootsocial.entity.User;
import com.bharat.springbootsocial.exception.UserException;
import com.bharat.springbootsocial.repository.CallRoomRepo;
import com.bharat.springbootsocial.repository.CallSessionRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class CallRoomServiceImpl implements CallRoomService {

    @Autowired
    private CallRoomRepo callRoomRepo;

    @Autowired
    private CallSessionRepo callSessionRepo;

    @Autowired
    private ServiceInt userService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private RoomEventService roomEventService;

    @Override
    public CallRoom createCallRoom(User creator, String roomName, CallRoom.CallType callType, List<UUID> participantIds) throws UserException {
        try {
            CallRoom room = new CallRoom();
            room.setRoomName(roomName);
            room.setCreatedBy(creator);
            room.setCallType(callType);
            room.setStatus(CallRoom.CallStatus.WAITING);
            room.setIsActive(true);

            room.addParticipant(creator);

            if (participantIds != null && !participantIds.isEmpty()) {
                for (UUID participantId : participantIds) {
                    User participant = userService.getUserById(participantId);
                    if (participant != null && !participant.getId().equals(creator.getId())) {
                        room.addParticipant(participant);
                    }
                }
            }

            int totalParticipants = (participantIds != null ? participantIds.size() : 0) + 1;
            if (callType == CallRoom.CallType.VOICE_ONLY || callType == CallRoom.CallType.VIDEO_CALL) {
                room.setMaxParticipants(Math.max(totalParticipants, 2));
            } else {
                room.setMaxParticipants(10);
            }

            CallRoom savedRoom = callRoomRepo.save(room);

            try {
                createCallSession(savedRoom, creator);
            } catch (Exception ignored) {
                // Session creation failure does not block room creation
            }

            if (participantIds != null && !participantIds.isEmpty()) {
                for (UUID participantId : participantIds) {
                    if (!participantId.equals(creator.getId())) {
                        User participant = userService.getUserById(participantId);
                        if (participant != null) {
                            notificationService.sendCallInvitationNotification(participant, creator, savedRoom);
                        }
                    }
                }
            }

            return savedRoom;
        } catch (Exception e) {
            throw new UserException("Failed to create call room: " + e.getMessage());
        }
    }

    @Override
    public CallRoom joinCallRoom(String roomId, User user) throws UserException {
        try {
            CallRoom room = getCallRoomById(roomId);

            if (!room.canJoin()) {
                if (room.getStatus() == CallRoom.CallStatus.ENDED && !room.getIsActive()) {
                    LocalDateTime roomAge = room.getCreatedAt();
                    if (roomAge != null && roomAge.isAfter(LocalDateTime.now().minusHours(2))) {
                        room.setStatus(CallRoom.CallStatus.WAITING);
                        room.setIsActive(true);
                        callRoomRepo.save(room);
                    } else {
                        throw new UserException("Cannot join room. Room has ended and is too old to reactivate.");
                    }
                } else if (room.getStatus() == CallRoom.CallStatus.WAITING && room.getIsActive() &&
                           room.getParticipants().size() >= room.getMaxParticipants()) {
                    if (room.getMaxParticipants() <= 4) {
                        room.setMaxParticipants(room.getMaxParticipants() + 2);
                        callRoomRepo.save(room);
                    } else {
                        throw new UserException("Cannot join room. Room is full.");
                    }
                } else {
                    throw new UserException("Cannot join room. Room status or participant count prevents joining.");
                }
            }

            if (!room.isParticipant(user)) {
                if (room.getParticipants().size() >= room.getMaxParticipants()) {
                    throw new UserException("Cannot join room. Room is full.");
                }
                room.addParticipant(user);
                callRoomRepo.save(room);
            }

            createOrUpdateCallSession(room, user);

            if (room.getStatus() == CallRoom.CallStatus.WAITING && room.getParticipants().size() >= 2) {
                room.setStatus(CallRoom.CallStatus.ACTIVE);
                callRoomRepo.save(room);
                roomEventService.sendRoomStatusChangedEvent(room, "WAITING", "ACTIVE");
                roomEventService.sendCustomRoomEvent(room, "webrtc-signaling-start", Map.of(
                        "message", "WebRTC signaling can now begin",
                        "participants", room.getParticipants().stream()
                                .map(p -> Map.of("id", p.getId(), "name", p.getFname() + " " + p.getLname()))
                                .toList()
                ));
            }

            roomEventService.sendUserJoinedEvent(room, user);
            roomEventService.sendParticipantListUpdate(room);

            return room;
        } catch (Exception e) {
            throw new UserException("Failed to join call room: " + e.getMessage());
        }
    }

    @Override
    public void leaveCallRoom(String roomId, User user) throws UserException {
        try {
            CallRoom room = getCallRoomById(roomId);

            Optional<CallSession> sessionOpt = callSessionRepo.findByRoomAndUser(room, user);
            sessionOpt.ifPresent(session -> {
                session.leaveSession();
                callSessionRepo.save(session);
            });

            room.removeParticipant(user);

            roomEventService.sendUserLeftEvent(room, user);

            if (room.getParticipants().isEmpty()) {
                room.endCall();
                roomEventService.sendRoomEndedEvent(room, user);
            } else if (room.getCreatedBy().getId().equals(user.getId()) && !room.getParticipants().isEmpty()) {
                room.setCreatedBy(room.getParticipants().get(0));
            }

            callRoomRepo.save(room);
            roomEventService.sendParticipantListUpdate(room);
        } catch (Exception e) {
            throw new UserException("Failed to leave call room: " + e.getMessage());
        }
    }

    @Override
    public void endCallRoom(String roomId, User user) throws UserException {
        try {
            CallRoom room = getCallRoomById(roomId);
            if (!room.getCreatedBy().getId().equals(user.getId())) {
                throw new UserException("Only room creator can end the call");
            }

            List<CallSession> activeSessions = callSessionRepo.findActiveSessionsByRoom(room);
            for (CallSession session : activeSessions) {
                session.leaveSession();
                callSessionRepo.save(session);
            }

            room.endCall();
            callRoomRepo.save(room);
            roomEventService.sendRoomEndedEvent(room, user);
        } catch (Exception e) {
            throw new UserException("Failed to end call room: " + e.getMessage());
        }
    }

    @Override
    public CallRoom getCallRoomById(String roomId) throws UserException {
        Optional<CallRoom> roomOpt = callRoomRepo.findByRoomId(roomId);
        if (roomOpt.isEmpty()) {
            try {
                UUID uuid = UUID.fromString(roomId);
                roomOpt = callRoomRepo.findById(uuid);
            } catch (IllegalArgumentException ignored) {
            }
        }
        if (roomOpt.isEmpty()) {
            throw new UserException("Call room not found");
        }
        return roomOpt.get();
    }

    @Override
    public List<CallRoom> getActiveRoomsForUser(User user) {
        return callRoomRepo.findActiveRoomsByParticipant(user);
    }

    @Override
    public List<CallRoom> getRoomsCreatedByUser(User user) {
        return callRoomRepo.findByCreatedBy(user);
    }

    @Override
    public CallRoom addParticipant(String roomId, User user, UUID participantId) throws UserException {
        CallRoom room = getCallRoomById(roomId);
        if (!room.getCreatedBy().getId().equals(user.getId())) {
            throw new UserException("Only room creator can add participants");
        }

        if (room.getParticipants().size() >= room.getMaxParticipants()) {
            throw new UserException("Room is full");
        }

        User participant = userService.getUserById(participantId);
        if (participant == null) {
            throw new UserException("Participant not found");
        }

        room.addParticipant(participant);
        return callRoomRepo.save(room);
    }

    @Override
    public CallRoom removeParticipant(String roomId, User user, UUID participantId) throws UserException {
        CallRoom room = getCallRoomById(roomId);
        if (!room.getCreatedBy().getId().equals(user.getId())) {
            throw new UserException("Only room creator can remove participants");
        }

        User participant = userService.getUserById(participantId);
        if (participant == null) {
            throw new UserException("Participant not found");
        }

        room.removeParticipant(participant);

        Optional<CallSession> sessionOpt = callSessionRepo.findByRoomAndUser(room, participant);
        sessionOpt.ifPresent(session -> {
            session.leaveSession();
            callSessionRepo.save(session);
        });

        return callRoomRepo.save(room);
    }

    @Override
    public CallRoom updateRoomStatus(String roomId, CallRoom.CallStatus status) throws UserException {
        CallRoom room = getCallRoomById(roomId);
        room.setStatus(status);
        return callRoomRepo.save(room);
    }

    @Override
    public boolean canUserJoinRoom(String roomId, User user) throws UserException {
        CallRoom room = getCallRoomById(roomId);
        return room.canJoin() && (room.isParticipant(user) || room.getParticipants().size() < room.getMaxParticipants());
    }

    @Override
    public List<User> getRoomParticipants(String roomId) throws UserException {
        CallRoom room = getCallRoomById(roomId);
        return room.getParticipants();
    }

    @Override
    @Scheduled(fixedRate = 300000)
    public void cleanupOldRooms() {
        try {
            LocalDateTime cutoffTime = LocalDateTime.now().minusHours(1);
            List<CallRoom> oldRooms = callRoomRepo.findRoomsForCleanup(cutoffTime);

            for (CallRoom room : oldRooms) {
                List<CallSession> sessions = callSessionRepo.findByRoom(room);
                for (CallSession session : sessions) {
                    session.leaveSession();
                    callSessionRepo.save(session);
                }
                room.endCall();
                callRoomRepo.save(room);
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public CallRoomStats getRoomStatistics(String roomId) throws UserException {
        CallRoom room = getCallRoomById(roomId);
        return new CallRoomStats(
                room.getRoomId(),
                room.getParticipants().size(),
                room.getDurationSeconds(),
                room.getStatus(),
                room.getIsActive()
        );
    }

    private void createCallSession(CallRoom room, User user) {
        try {
            Optional<CallSession> existingSession = callSessionRepo.findByRoomAndUser(room, user);
            if (existingSession.isPresent()) {
                CallSession session = existingSession.get();
                session.setStatus(CallSession.SessionStatus.JOINING);
                session.updateActivity();
                callSessionRepo.save(session);
                return;
            }

            CallSession session = new CallSession();
            session.setRoom(room);
            session.setUser(user);
            session.setStatus(CallSession.SessionStatus.JOINING);
            callSessionRepo.save(session);
        } catch (Exception e) {
            try {
                Optional<CallSession> existingSession = callSessionRepo.findByRoomAndUser(room, user);
                existingSession.ifPresent(session -> {
                    session.setStatus(CallSession.SessionStatus.JOINING);
                    session.updateActivity();
                    callSessionRepo.save(session);
                });
            } catch (Exception ignored) {
            }
        }
    }

    private void createOrUpdateCallSession(CallRoom room, User user) {
        try {
            Optional<CallSession> sessionOpt = callSessionRepo.findByRoomAndUser(room, user);

            if (!sessionOpt.isPresent()) {
                List<CallSession> sessionsInRoom = callSessionRepo.findByRoom(room);
                for (CallSession session : sessionsInRoom) {
                    if (session.getUser().getId().equals(user.getId())) {
                        sessionOpt = Optional.of(session);
                        break;
                    }
                }
            }

            if (sessionOpt.isPresent()) {
                CallSession session = sessionOpt.get();
                session.setStatus(CallSession.SessionStatus.JOINING);
                session.updateActivity();
                callSessionRepo.save(session);
            } else {
                createCallSession(room, user);
            }
        } catch (Exception ignored) {
        }
    }
}
