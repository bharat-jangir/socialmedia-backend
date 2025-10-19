package com.bharat.springbootsocial.controller;

import com.bharat.springbootsocial.entity.Group;
import com.bharat.springbootsocial.entity.GroupCallRoom;
import com.bharat.springbootsocial.entity.User;
import java.util.UUID;
import com.bharat.springbootsocial.exception.UserException;
import com.bharat.springbootsocial.response.ApiResponse;
import com.bharat.springbootsocial.services.GroupCallService;
import com.bharat.springbootsocial.services.ServiceInt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/group-calls")
@CrossOrigin(origins = "*")
public class GroupCallController {
    
    @Autowired
    private GroupCallService groupCallService;
    
    @Autowired
    private ServiceInt userService;
    
    // Create a new group call room
    @PostMapping("/create")
    public ResponseEntity<ApiResponse> createGroupCallRoom(
            @RequestHeader("Authorization") String jwt,
            @RequestBody CreateGroupCallRoomRequest request) {
        try {
            User currentUser = userService.getUserFromToken(jwt);
            
            // Validate request
            if (request.getGroupId() == null) {
                return new ResponseEntity<>(
                    new ApiResponse("Group ID is required", false),
                    HttpStatus.BAD_REQUEST
                );
            }
            
            if (request.getCallType() == null) {
                return new ResponseEntity<>(
                    new ApiResponse("Call type is required", false),
                    HttpStatus.BAD_REQUEST
                );
            }
            
            GroupCallRoom room = groupCallService.createGroupCallRoom(
                currentUser,
                request.getGroupId(),
                request.getCallType(),
                request.getRoomName()
            );
            
            return new ResponseEntity<>(
                new ApiResponse("Group call room created successfully", true, room),
                HttpStatus.CREATED
            );
            
        } catch (UserException e) {
            return new ResponseEntity<>(
                new ApiResponse(e.getMessage(), false),
                HttpStatus.BAD_REQUEST
            );
        } catch (Exception e) {
            return new ResponseEntity<>(
                new ApiResponse("Failed to create group call room: " + e.getMessage(), false),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }
    
    // Join an existing group call room
    @PostMapping("/join/{roomId}")
    public ResponseEntity<ApiResponse> joinGroupCallRoom(
            @RequestHeader("Authorization") String jwt,
            @PathVariable String roomId) {
        try {
            User currentUser = userService.getUserFromToken(jwt);
            
            GroupCallRoom room = groupCallService.joinGroupCallRoom(roomId, currentUser);
            
            return new ResponseEntity<>(
                new ApiResponse("Joined group call room successfully", true, room),
                HttpStatus.OK
            );
            
        } catch (UserException e) {
            return new ResponseEntity<>(
                new ApiResponse(e.getMessage(), false),
                HttpStatus.BAD_REQUEST
            );
        } catch (Exception e) {
            return new ResponseEntity<>(
                new ApiResponse("Failed to join group call room: " + e.getMessage(), false),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }
    
    // Leave a group call room
    @PostMapping("/leave/{roomId}")
    public ResponseEntity<ApiResponse> leaveGroupCallRoom(
            @RequestHeader("Authorization") String jwt,
            @PathVariable String roomId) {
        try {
            User currentUser = userService.getUserFromToken(jwt);
            
            groupCallService.leaveGroupCallRoom(roomId, currentUser);
            
            return new ResponseEntity<>(
                new ApiResponse("Left group call room successfully", true),
                HttpStatus.OK
            );
            
        } catch (UserException e) {
            return new ResponseEntity<>(
                new ApiResponse(e.getMessage(), false),
                HttpStatus.BAD_REQUEST
            );
        } catch (Exception e) {
            return new ResponseEntity<>(
                new ApiResponse("Failed to leave group call room: " + e.getMessage(), false),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }
    
    // End a group call room
    @PostMapping("/end/{roomId}")
    public ResponseEntity<ApiResponse> endGroupCallRoom(
            @RequestHeader("Authorization") String jwt,
            @PathVariable String roomId) {
        try {
            User currentUser = userService.getUserFromToken(jwt);
            
            groupCallService.endGroupCallRoom(roomId, currentUser);
            
            return new ResponseEntity<>(
                new ApiResponse("Group call room ended successfully", true),
                HttpStatus.OK
            );
            
        } catch (UserException e) {
            return new ResponseEntity<>(
                new ApiResponse(e.getMessage(), false),
                HttpStatus.BAD_REQUEST
            );
        } catch (Exception e) {
            return new ResponseEntity<>(
                new ApiResponse("Failed to end group call room: " + e.getMessage(), false),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }
    
    // Get group call room details
    @GetMapping("/room/{roomId}")
    public ResponseEntity<ApiResponse> getGroupCallRoom(
            @RequestHeader("Authorization") String jwt,
            @PathVariable String roomId) {
        try {
            User currentUser = userService.getUserFromToken(jwt);
            
            GroupCallRoom room = groupCallService.getGroupCallRoomById(roomId);
            
            // Check if user is authorized to view this room
            if (!room.isParticipant(currentUser)) {
                return new ResponseEntity<>(
                    new ApiResponse("Not authorized to view this room", false),
                    HttpStatus.FORBIDDEN
                );
            }
            
            return new ResponseEntity<>(
                new ApiResponse("Group call room retrieved successfully", true, room),
                HttpStatus.OK
            );
            
        } catch (UserException e) {
            return new ResponseEntity<>(
                new ApiResponse(e.getMessage(), false),
                HttpStatus.NOT_FOUND
            );
        } catch (Exception e) {
            return new ResponseEntity<>(
                new ApiResponse("Failed to get group call room: " + e.getMessage(), false),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }
    
    // Get active group call rooms for a group
    @GetMapping("/group/{groupId}/active")
    public ResponseEntity<ApiResponse> getActiveGroupCallRooms(
            @RequestHeader("Authorization") String jwt,
            @PathVariable UUID groupId) {
        try {
            User currentUser = userService.getUserFromToken(jwt);
            
            List<GroupCallRoom> activeRooms = groupCallService.getActiveGroupCallRooms(groupId);
            
            return new ResponseEntity<>(
                new ApiResponse("Active group call rooms retrieved successfully", true, activeRooms),
                HttpStatus.OK
            );
            
        } catch (Exception e) {
            return new ResponseEntity<>(
                new ApiResponse("Failed to get active group call rooms: " + e.getMessage(), false),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }
    
    
    // Check if user can join group call room
    @GetMapping("/room/{roomId}/can-join")
    public ResponseEntity<ApiResponse> canJoinGroupCallRoom(
            @RequestHeader("Authorization") String jwt,
            @PathVariable String roomId) {
        try {
            User currentUser = userService.getUserFromToken(jwt);
            
            boolean canJoin = groupCallService.canUserJoinGroupCallRoom(roomId, currentUser);
            
            return new ResponseEntity<>(
                new ApiResponse("Join permission checked", true, Map.of("canJoin", canJoin)),
                HttpStatus.OK
            );
            
        } catch (UserException e) {
            return new ResponseEntity<>(
                new ApiResponse(e.getMessage(), false),
                HttpStatus.NOT_FOUND
            );
        } catch (Exception e) {
            return new ResponseEntity<>(
                new ApiResponse("Failed to check join permission: " + e.getMessage(), false),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }
    
    // Start group call
    @PostMapping("/start/{roomId}")
    public ResponseEntity<ApiResponse> startGroupCall(
            @RequestHeader("Authorization") String jwt,
            @PathVariable String roomId) {
        try {
            User currentUser = userService.getUserFromToken(jwt);
            
            GroupCallRoom room = groupCallService.startGroupCall(roomId, currentUser);
            
            return new ResponseEntity<>(
                new ApiResponse("Group call started successfully", true, room),
                HttpStatus.OK
            );
            
        } catch (UserException e) {
            return new ResponseEntity<>(
                new ApiResponse(e.getMessage(), false),
                HttpStatus.BAD_REQUEST
            );
        } catch (Exception e) {
            return new ResponseEntity<>(
                new ApiResponse("Failed to start group call: " + e.getMessage(), false),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }
    
    // DTO for creating group call room
    public static class CreateGroupCallRoomRequest {
        private UUID groupId;
        private String roomName;
        private GroupCallRoom.CallType callType;
        
        // Constructors
        public CreateGroupCallRoomRequest() {}
        
        public CreateGroupCallRoomRequest(UUID groupId, String roomName, GroupCallRoom.CallType callType) {
            this.groupId = groupId;
            this.roomName = roomName;
            this.callType = callType;
        }
        
        // Getters and setters
        public UUID getGroupId() { return groupId; }
        public void setGroupId(UUID groupId) { this.groupId = groupId; }
        
        public String getRoomName() { return roomName; }
        public void setRoomName(String roomName) { this.roomName = roomName; }
        
        public GroupCallRoom.CallType getCallType() { return callType; }
        public void setCallType(GroupCallRoom.CallType callType) { this.callType = callType; }
    }
    
    // Get room participants
    @GetMapping("/room/{roomId}/participants")
    public ResponseEntity<ApiResponse> getRoomParticipants(
            @RequestHeader("Authorization") String jwt,
            @PathVariable String roomId) {
        try {
            User currentUser = userService.getUserFromToken(jwt);
            
            GroupCallRoom room = groupCallService.getGroupCallRoomById(roomId);
            
            // Check if user is authorized to view this room
            if (!room.isParticipant(currentUser)) {
                return new ResponseEntity<>(
                    new ApiResponse("Not authorized to view this room", false),
                    HttpStatus.FORBIDDEN
                );
            }
            
            List<User> participants = groupCallService.getRoomParticipants(roomId);
            return new ResponseEntity<>(
                new ApiResponse("Room participants retrieved successfully", true, participants),
                HttpStatus.OK
            );
        } catch (UserException e) {
            return new ResponseEntity<>(
                new ApiResponse(e.getMessage(), false),
                HttpStatus.NOT_FOUND
            );
        } catch (Exception e) {
            return new ResponseEntity<>(
                new ApiResponse("Failed to get room participants: " + e.getMessage(), false),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }
}
