package com.bharat.springbootsocial.controller;

import com.bharat.springbootsocial.entity.GroupMember;
import com.bharat.springbootsocial.entity.User;
import java.util.UUID;
import com.bharat.springbootsocial.repository.GroupMemberRepo;
import com.bharat.springbootsocial.response.GroupMessageWebSocketResponse;
import com.bharat.springbootsocial.services.ServiceInt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/test/websocket")
@CrossOrigin(origins = "http://localhost:5173")
public class GroupMessageWebSocketTestController {
    
    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;
    
    @Autowired
    private GroupMemberRepo groupMemberRepo;
    
    @Autowired
    private ServiceInt userService;
    
    // Test endpoint to check group members
    @GetMapping("/group/{groupId}/members")
    public List<GroupMember> getGroupMembers(@PathVariable UUID groupId) {
        return groupMemberRepo.findByGroupIdAndStatus(groupId, GroupMember.MemberStatus.ACTIVE);
    }
    
    // Test endpoint to simulate typing indicator
    @PostMapping("/group/{groupId}/typing/test")
    public String testTypingIndicator(
            @RequestHeader("Authorization") String jwt,
            @PathVariable UUID groupId,
            @RequestParam String action) {
        try {
            User sender = userService.getUserFromToken(jwt);
            
            GroupMessageWebSocketResponse response = new GroupMessageWebSocketResponse();
            response.setGroupId(groupId);
            response.setAction(action);
            response.setSender(convertToUserResponse(sender));
            
            // Get all group members
            List<GroupMember> members = groupMemberRepo.findByGroupIdAndStatus(groupId, GroupMember.MemberStatus.ACTIVE);
            
            int sentCount = 0;
            for (GroupMember member : members) {
                // Skip the sender
                if (!member.getUser().getId().equals(sender.getId())) {
                    // Send typing indicator to each member individually
                    simpMessagingTemplate.convertAndSendToUser(
                        member.getUser().getId().toString(), 
                        "/queue/typing", 
                        response
                    );
                    sentCount++;
                }
            }
            
            return String.format("Typing indicator sent to %d members (excluding sender %d)", sentCount, sender.getId());
            
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
    
    // Test endpoint to broadcast to group (for comparison)
    @PostMapping("/group/{groupId}/broadcast/test")
    public String testGroupBroadcast(
            @RequestHeader("Authorization") String jwt,
            @PathVariable UUID groupId,
            @RequestParam String action) {
        try {
            User sender = userService.getUserFromToken(jwt);
            
            GroupMessageWebSocketResponse response = new GroupMessageWebSocketResponse();
            response.setGroupId(groupId);
            response.setAction(action);
            response.setSender(convertToUserResponse(sender));
            
            // Broadcast to all group members (including sender)
            simpMessagingTemplate.convertAndSend("/group/" + groupId, response);
            
            return "Group broadcast sent (including sender)";
            
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
    
    private GroupMessageWebSocketResponse.UserResponse convertToUserResponse(User user) {
        GroupMessageWebSocketResponse.UserResponse userResponse = new GroupMessageWebSocketResponse.UserResponse();
        userResponse.setId(user.getId());
        userResponse.setFirstName(user.getFname());
        userResponse.setLastName(user.getLname());
        userResponse.setEmail(user.getEmail());
        userResponse.setProfileImage(user.getProfileImage());
        return userResponse;
    }
}
