package com.bharat.springbootsocial.controller;

import com.bharat.springbootsocial.entity.CallRoom;
import com.bharat.springbootsocial.entity.User;
import java.util.UUID;
import com.bharat.springbootsocial.exception.UserException;
import com.bharat.springbootsocial.response.ApiResponse;
import com.bharat.springbootsocial.services.CallRoomService;
import com.bharat.springbootsocial.services.ServiceInt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/calls")
@CrossOrigin(origins = "*")
public class CallRoomController {
    
    @Autowired
    private CallRoomService callRoomService;
    
    @Autowired
    private ServiceInt userService;
    
    // Create a new call room
    @PostMapping("/create")
    public ResponseEntity<ApiResponse> createCallRoom(
            @RequestHeader("Authorization") String jwt,
            @RequestBody CreateCallRoomRequest request) {
        try {
            User currentUser = userService.getUserFromToken(jwt);
            
            // Validate request
            if (request.getRoomName() == null || request.getRoomName().trim().isEmpty()) {
                return new ResponseEntity<>(
                    new ApiResponse("Room name is required", false),
                    HttpStatus.BAD_REQUEST
                );
            }
            
            if (request.getCallType() == null) {
                return new ResponseEntity<>(
                    new ApiResponse("Call type is required", false),
                    HttpStatus.BAD_REQUEST
                );
            }
            
            CallRoom room = callRoomService.createCallRoom(
                currentUser,
                request.getRoomName().trim(),
                request.getCallType(),
                request.getParticipantIds()
            );
            
            // Debug logging
            System.out.println("=== DEBUG: CallRoom Created ===");
            System.out.println("Room ID (UUID): " + room.getId());
            System.out.println("Room ID (String): " + room.getRoomId());
            System.out.println("Room Name: " + room.getRoomName());
            System.out.println("Call Type: " + room.getCallType());
            System.out.println("Status: " + room.getStatus());
            System.out.println("Is Active: " + room.getIsActive());
            System.out.println("Max Participants: " + room.getMaxParticipants());
            System.out.println("Participants Count: " + room.getParticipants().size());
            System.out.println("===============================");
            
            return new ResponseEntity<>(
                new ApiResponse("Call room created successfully", true, room),
                HttpStatus.CREATED
            );
            
        } catch (UserException e) {
            return new ResponseEntity<>(
                new ApiResponse(e.getMessage(), false),
                HttpStatus.BAD_REQUEST
            );
        } catch (Exception e) {
            return new ResponseEntity<>(
                new ApiResponse("Failed to create call room: " + e.getMessage(), false),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }
    
    // Join an existing call room
    @PostMapping("/join/{roomId}")
    public ResponseEntity<ApiResponse> joinCallRoom(
            @RequestHeader("Authorization") String jwt,
            @PathVariable String roomId) {
        try {
            User currentUser = userService.getUserFromToken(jwt);
            
            CallRoom room = callRoomService.joinCallRoom(roomId, currentUser);
            
            return new ResponseEntity<>(
                new ApiResponse("Joined call room successfully", true, room),
                HttpStatus.OK
            );
            
        } catch (UserException e) {
            return new ResponseEntity<>(
                new ApiResponse(e.getMessage(), false),
                HttpStatus.BAD_REQUEST
            );
        } catch (Exception e) {
            return new ResponseEntity<>(
                new ApiResponse("Failed to join call room: " + e.getMessage(), false),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }
    
    // Leave a call room
    @PostMapping("/leave/{roomId}")
    public ResponseEntity<ApiResponse> leaveCallRoom(
            @RequestHeader("Authorization") String jwt,
            @PathVariable String roomId) {
        try {
            User currentUser = userService.getUserFromToken(jwt);
            
            callRoomService.leaveCallRoom(roomId, currentUser);
            
            return new ResponseEntity<>(
                new ApiResponse("Left call room successfully", true),
                HttpStatus.OK
            );
            
        } catch (UserException e) {
            return new ResponseEntity<>(
                new ApiResponse(e.getMessage(), false),
                HttpStatus.BAD_REQUEST
            );
        } catch (Exception e) {
            return new ResponseEntity<>(
                new ApiResponse("Failed to leave call room: " + e.getMessage(), false),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }
    
    // End a call room
    @PostMapping("/end/{roomId}")
    public ResponseEntity<ApiResponse> endCallRoom(
            @RequestHeader("Authorization") String jwt,
            @PathVariable String roomId) {
        try {
            User currentUser = userService.getUserFromToken(jwt);
            
            callRoomService.endCallRoom(roomId, currentUser);
            
            return new ResponseEntity<>(
                new ApiResponse("Call room ended successfully", true),
                HttpStatus.OK
            );
            
        } catch (UserException e) {
            return new ResponseEntity<>(
                new ApiResponse(e.getMessage(), false),
                HttpStatus.BAD_REQUEST
            );
        } catch (Exception e) {
            return new ResponseEntity<>(
                new ApiResponse("Failed to end call room: " + e.getMessage(), false),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }
    
    // Get call room details
    @GetMapping("/room/{roomId}")
    public ResponseEntity<ApiResponse> getCallRoom(
            @RequestHeader("Authorization") String jwt,
            @PathVariable String roomId) {
        try {
            User currentUser = userService.getUserFromToken(jwt);
            
            CallRoom room = callRoomService.getCallRoomById(roomId);
            
            // Check if user is authorized to view this room
            if (!room.isParticipant(currentUser)) {
                return new ResponseEntity<>(
                    new ApiResponse("Not authorized to view this room", false),
                    HttpStatus.FORBIDDEN
                );
            }
            
            return new ResponseEntity<>(
                new ApiResponse("Call room retrieved successfully", true, room),
                HttpStatus.OK
            );
            
        } catch (UserException e) {
            return new ResponseEntity<>(
                new ApiResponse(e.getMessage(), false),
                HttpStatus.NOT_FOUND
            );
        } catch (Exception e) {
            return new ResponseEntity<>(
                new ApiResponse("Failed to get call room: " + e.getMessage(), false),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }
    
    // Get active rooms for current user
    @GetMapping("/active")
    public ResponseEntity<ApiResponse> getActiveRooms(
            @RequestHeader("Authorization") String jwt) {
        try {
            User currentUser = userService.getUserFromToken(jwt);
            
            List<CallRoom> activeRooms = callRoomService.getActiveRoomsForUser(currentUser);
            
            return new ResponseEntity<>(
                new ApiResponse("Active rooms retrieved successfully", true, activeRooms),
                HttpStatus.OK
            );
            
        } catch (Exception e) {
            return new ResponseEntity<>(
                new ApiResponse("Failed to get active rooms: " + e.getMessage(), false),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }
    
    // Get rooms created by current user
    @GetMapping("/created")
    public ResponseEntity<ApiResponse> getCreatedRooms(
            @RequestHeader("Authorization") String jwt) {
        try {
            User currentUser = userService.getUserFromToken(jwt);
            
            List<CallRoom> createdRooms = callRoomService.getRoomsCreatedByUser(currentUser);
            
            return new ResponseEntity<>(
                new ApiResponse("Created rooms retrieved successfully", true, createdRooms),
                HttpStatus.OK
            );
            
        } catch (Exception e) {
            return new ResponseEntity<>(
                new ApiResponse("Failed to get created rooms: " + e.getMessage(), false),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }
    
    // Add participant to room
    @PostMapping("/room/{roomId}/add-participant")
    public ResponseEntity<ApiResponse> addParticipant(
            @RequestHeader("Authorization") String jwt,
            @PathVariable String roomId,
            @RequestBody Map<String, UUID> request) {
        try {
            User currentUser = userService.getUserFromToken(jwt);
            UUID participantId = request.get("participantId");
            
            if (participantId == null) {
                return new ResponseEntity<>(
                    new ApiResponse("Participant ID is required", false),
                    HttpStatus.BAD_REQUEST
                );
            }
            
            CallRoom room = callRoomService.addParticipant(roomId, currentUser, participantId);
            
            return new ResponseEntity<>(
                new ApiResponse("Participant added successfully", true, room),
                HttpStatus.OK
            );
            
        } catch (UserException e) {
            return new ResponseEntity<>(
                new ApiResponse(e.getMessage(), false),
                HttpStatus.BAD_REQUEST
            );
        } catch (Exception e) {
            return new ResponseEntity<>(
                new ApiResponse("Failed to add participant: " + e.getMessage(), false),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }
    
    // Remove participant from room
    @PostMapping("/room/{roomId}/remove-participant")
    public ResponseEntity<ApiResponse> removeParticipant(
            @RequestHeader("Authorization") String jwt,
            @PathVariable String roomId,
            @RequestBody Map<String, UUID> request) {
        try {
            User currentUser = userService.getUserFromToken(jwt);
            UUID participantId = request.get("participantId");
            
            if (participantId == null) {
                return new ResponseEntity<>(
                    new ApiResponse("Participant ID is required", false),
                    HttpStatus.BAD_REQUEST
                );
            }
            
            CallRoom room = callRoomService.removeParticipant(roomId, currentUser, participantId);
            
            return new ResponseEntity<>(
                new ApiResponse("Participant removed successfully", true, room),
                HttpStatus.OK
            );
            
        } catch (UserException e) {
            return new ResponseEntity<>(
                new ApiResponse(e.getMessage(), false),
                HttpStatus.BAD_REQUEST
            );
        } catch (Exception e) {
            return new ResponseEntity<>(
                new ApiResponse("Failed to remove participant: " + e.getMessage(), false),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }
    
    // Get room participants
    @GetMapping("/room/{roomId}/participants")
    public ResponseEntity<ApiResponse> getRoomParticipants(
            @RequestHeader("Authorization") String jwt,
            @PathVariable String roomId) {
        try {
            // User currentUser = userService.getUserFromToken(jwt);
            
            List<User> participants = callRoomService.getRoomParticipants(roomId);
            
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
    
    // Get room statistics
    @GetMapping("/room/{roomId}/stats")
    public ResponseEntity<ApiResponse> getRoomStatistics(
            @RequestHeader("Authorization") String jwt,
            @PathVariable String roomId) {
        try {
            // User currentUser = userService.getUserFromToken(jwt);
            
            CallRoomService.CallRoomStats stats = callRoomService.getRoomStatistics(roomId);
            
            return new ResponseEntity<>(
                new ApiResponse("Room statistics retrieved successfully", true, stats),
                HttpStatus.OK
            );
            
        } catch (UserException e) {
            return new ResponseEntity<>(
                new ApiResponse(e.getMessage(), false),
                HttpStatus.NOT_FOUND
            );
        } catch (Exception e) {
            return new ResponseEntity<>(
                new ApiResponse("Failed to get room statistics: " + e.getMessage(), false),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }
    
    // Check if user can join room
    @GetMapping("/room/{roomId}/can-join")
    public ResponseEntity<ApiResponse> canJoinRoom(
            @RequestHeader("Authorization") String jwt,
            @PathVariable String roomId) {
        try {
            User currentUser = userService.getUserFromToken(jwt);
            
            boolean canJoin = callRoomService.canUserJoinRoom(roomId, currentUser);
            
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
    
    // DTO for creating call room
    public static class CreateCallRoomRequest {
        private String roomName;
        private CallRoom.CallType callType;
        private List<UUID> participantIds;
        
        // Constructors
        public CreateCallRoomRequest() {}
        
        public CreateCallRoomRequest(String roomName, CallRoom.CallType callType, List<UUID> participantIds) {
            this.roomName = roomName;
            this.callType = callType;
            this.participantIds = participantIds;
        }
        
        // Getters and setters
        public String getRoomName() { return roomName; }
        public void setRoomName(String roomName) { this.roomName = roomName; }
        
        public CallRoom.CallType getCallType() { return callType; }
        public void setCallType(CallRoom.CallType callType) { this.callType = callType; }
        
        public List<UUID> getParticipantIds() { return participantIds; }
        public void setParticipantIds(List<UUID> participantIds) { this.participantIds = participantIds; }
    }
}
