package com.bharat.springbootsocial.controller;

import com.bharat.springbootsocial.entity.Group;
import com.bharat.springbootsocial.entity.GroupMember;
import com.bharat.springbootsocial.entity.User;
import java.util.UUID;
import com.bharat.springbootsocial.response.ApiResponse;
import com.bharat.springbootsocial.response.PaginatedResponse;
import com.bharat.springbootsocial.services.GroupService;
import com.bharat.springbootsocial.services.ServiceInt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/groups")
@CrossOrigin(origins = "http://localhost:5173")
public class GroupController {
    
    @Autowired
    private GroupService groupService;
    
    @Autowired
    private ServiceInt userService;
    
    // Group CRUD operations
    @PostMapping
    public ResponseEntity<ApiResponse> createGroup(
            @RequestHeader("Authorization") String jwt,
            @RequestBody CreateGroupRequest request) {
        try {
            User creator = userService.getUserFromToken(jwt);
            
            Group group = groupService.createGroup(
                creator,
                request.getName(),
                request.getDescription(),
                request.getGroupType(),
                request.getIsPublic(),
                request.getMemberIds()
            );
            
            return new ResponseEntity<>(
                new ApiResponse("Group created successfully", true, group),
                HttpStatus.CREATED
            );
            
        } catch (Exception e) {
            return new ResponseEntity<>(
                new ApiResponse("Failed to create group: " + e.getMessage(), false),
                HttpStatus.BAD_REQUEST
            );
        }
    }
    
    @PutMapping("/{groupId}")
    public ResponseEntity<ApiResponse> updateGroup(
            @RequestHeader("Authorization") String jwt,
            @PathVariable UUID groupId,
            @RequestBody UpdateGroupRequest request) {
        try {
            User user = userService.getUserFromToken(jwt);
            
            Group group = groupService.updateGroup(
                groupId,
                user,
                request.getName(),
                request.getDescription(),
                request.getGroupImage(),
                request.getGroupCoverImage()
            );
            
            return new ResponseEntity<>(
                new ApiResponse("Group updated successfully", true, group),
                HttpStatus.OK
            );
            
        } catch (Exception e) {
            return new ResponseEntity<>(
                new ApiResponse("Failed to update group: " + e.getMessage(), false),
                HttpStatus.BAD_REQUEST
            );
        }
    }
    
    @DeleteMapping("/{groupId}")
    public ResponseEntity<ApiResponse> deleteGroup(
            @RequestHeader("Authorization") String jwt,
            @PathVariable UUID groupId) {
        try {
            User user = userService.getUserFromToken(jwt);
            
            groupService.deleteGroup(groupId, user);
            
            return new ResponseEntity<>(
                new ApiResponse("Group deleted successfully", true),
                HttpStatus.OK
            );
            
        } catch (Exception e) {
            return new ResponseEntity<>(
                new ApiResponse("Failed to delete group: " + e.getMessage(), false),
                HttpStatus.BAD_REQUEST
            );
        }
    }
    
    @GetMapping("/{groupId}")
    public ResponseEntity<ApiResponse> getGroupById(@PathVariable UUID groupId) {
        try {
            Group group = groupService.getGroupById(groupId);
            
            return new ResponseEntity<>(
                new ApiResponse("Group retrieved successfully", true, group),
                HttpStatus.OK
            );
            
        } catch (Exception e) {
            return new ResponseEntity<>(
                new ApiResponse("Failed to get group: " + e.getMessage(), false),
                HttpStatus.NOT_FOUND
            );
        }
    }
    
    @GetMapping
    public ResponseEntity<ApiResponse> getUserGroups(
            @RequestHeader("Authorization") String jwt) {
        try {
            User user = userService.getUserFromToken(jwt);
            List<Group> groups = groupService.getGroupsByUserId(user.getId());
            
            return new ResponseEntity<>(
                new ApiResponse("User groups retrieved successfully", true, groups),
                HttpStatus.OK
            );
            
        } catch (Exception e) {
            return new ResponseEntity<>(
                new ApiResponse("Failed to get user groups: " + e.getMessage(), false),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }
    
    @GetMapping("/paginated")
    public ResponseEntity<ApiResponse> getUserGroupsPaginated(
            @RequestHeader("Authorization") String jwt,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            User user = userService.getUserFromToken(jwt);
            PaginatedResponse<Group> groups = groupService.getGroupsByUserIdPaginated(user.getId(), page, size);
            
            return new ResponseEntity<>(
                new ApiResponse("User groups retrieved successfully", true, groups),
                HttpStatus.OK
            );
            
        } catch (Exception e) {
            return new ResponseEntity<>(
                new ApiResponse("Failed to get user groups: " + e.getMessage(), false),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }
    
    // Group membership operations
    @PostMapping("/{groupId}/join")
    public ResponseEntity<ApiResponse> joinGroup(
            @RequestHeader("Authorization") String jwt,
            @PathVariable UUID groupId) {
        try {
            User user = userService.getUserFromToken(jwt);
            
            GroupMember member = groupService.joinGroup(groupId, user);
            
            return new ResponseEntity<>(
                new ApiResponse("Joined group successfully", true, member),
                HttpStatus.OK
            );
            
        } catch (Exception e) {
            return new ResponseEntity<>(
                new ApiResponse("Failed to join group: " + e.getMessage(), false),
                HttpStatus.BAD_REQUEST
            );
        }
    }
    
    @PostMapping("/{groupId}/leave")
    public ResponseEntity<ApiResponse> leaveGroup(
            @RequestHeader("Authorization") String jwt,
            @PathVariable UUID groupId) {
        try {
            User user = userService.getUserFromToken(jwt);
            
            groupService.leaveGroup(groupId, user);
            
            return new ResponseEntity<>(
                new ApiResponse("Left group successfully", true),
                HttpStatus.OK
            );
            
        } catch (Exception e) {
            return new ResponseEntity<>(
                new ApiResponse("Failed to leave group: " + e.getMessage(), false),
                HttpStatus.BAD_REQUEST
            );
        }
    }
    
    @PostMapping("/{groupId}/members")
    public ResponseEntity<ApiResponse> addMember(
            @RequestHeader("Authorization") String jwt,
            @PathVariable UUID groupId,
            @RequestBody Map<String, UUID> request) {
        try {
            User admin = userService.getUserFromToken(jwt);
            UUID memberId = request.get("memberId");
            
            if (memberId == null) {
                return new ResponseEntity<>(
                    new ApiResponse("Member ID is required", false),
                    HttpStatus.BAD_REQUEST
                );
            }
            
            GroupMember member = groupService.addMember(groupId, admin, memberId);
            
            return new ResponseEntity<>(
                new ApiResponse("Member added successfully", true, member),
                HttpStatus.OK
            );
            
        } catch (Exception e) {
            return new ResponseEntity<>(
                new ApiResponse("Failed to add member: " + e.getMessage(), false),
                HttpStatus.BAD_REQUEST
            );
        }
    }
    
    @DeleteMapping("/{groupId}/members/{memberId}")
    public ResponseEntity<ApiResponse> removeMember(
            @RequestHeader("Authorization") String jwt,
            @PathVariable UUID groupId,
            @PathVariable UUID memberId) {
        try {
            User admin = userService.getUserFromToken(jwt);
            
            groupService.removeMember(groupId, admin, memberId);
            
            return new ResponseEntity<>(
                new ApiResponse("Member removed successfully", true),
                HttpStatus.OK
            );
            
        } catch (Exception e) {
            return new ResponseEntity<>(
                new ApiResponse("Failed to remove member: " + e.getMessage(), false),
                HttpStatus.BAD_REQUEST
            );
        }
    }
    
    @PutMapping("/{groupId}/members/{memberId}/role")
    public ResponseEntity<ApiResponse> updateMemberRole(
            @RequestHeader("Authorization") String jwt,
            @PathVariable UUID groupId,
            @PathVariable UUID memberId,
            @RequestBody Map<String, String> request) {
        try {
            User admin = userService.getUserFromToken(jwt);
            String roleStr = request.get("role");
            
            if (roleStr == null) {
                return new ResponseEntity<>(
                    new ApiResponse("Role is required", false),
                    HttpStatus.BAD_REQUEST
                );
            }
            
            GroupMember.MemberRole role = GroupMember.MemberRole.valueOf(roleStr.toUpperCase());
            GroupMember member = groupService.updateMemberRole(groupId, admin, memberId, role);
            
            return new ResponseEntity<>(
                new ApiResponse("Member role updated successfully", true, member),
                HttpStatus.OK
            );
            
        } catch (Exception e) {
            return new ResponseEntity<>(
                new ApiResponse("Failed to update member role: " + e.getMessage(), false),
                HttpStatus.BAD_REQUEST
            );
        }
    }
    
    @PutMapping("/{groupId}/members/{memberId}/mute")
    public ResponseEntity<ApiResponse> muteMember(
            @RequestHeader("Authorization") String jwt,
            @PathVariable UUID groupId,
            @PathVariable UUID memberId,
            @RequestBody Map<String, Boolean> request) {
        try {
            User admin = userService.getUserFromToken(jwt);
            Boolean isMuted = request.get("isMuted");
            
            if (isMuted == null) {
                return new ResponseEntity<>(
                    new ApiResponse("isMuted flag is required", false),
                    HttpStatus.BAD_REQUEST
                );
            }
            
            groupService.muteMember(groupId, admin, memberId, isMuted);
            
            return new ResponseEntity<>(
                new ApiResponse("Member mute status updated successfully", true),
                HttpStatus.OK
            );
            
        } catch (Exception e) {
            return new ResponseEntity<>(
                new ApiResponse("Failed to update member mute status: " + e.getMessage(), false),
                HttpStatus.BAD_REQUEST
            );
        }
    }
    
    @PutMapping("/{groupId}/members/{memberId}/pin")
    public ResponseEntity<ApiResponse> pinMember(
            @RequestHeader("Authorization") String jwt,
            @PathVariable UUID groupId,
            @PathVariable UUID memberId,
            @RequestBody Map<String, Boolean> request) {
        try {
            User admin = userService.getUserFromToken(jwt);
            Boolean isPinned = request.get("isPinned");
            
            if (isPinned == null) {
                return new ResponseEntity<>(
                    new ApiResponse("isPinned flag is required", false),
                    HttpStatus.BAD_REQUEST
                );
            }
            
            groupService.pinMember(groupId, admin, memberId, isPinned);
            
            return new ResponseEntity<>(
                new ApiResponse("Member pin status updated successfully", true),
                HttpStatus.OK
            );
            
        } catch (Exception e) {
            return new ResponseEntity<>(
                new ApiResponse("Failed to update member pin status: " + e.getMessage(), false),
                HttpStatus.BAD_REQUEST
            );
        }
    }
    
    // Group member queries
    @GetMapping("/{groupId}/members")
    public ResponseEntity<ApiResponse> getGroupMembers(@PathVariable UUID groupId) {
        try {
            List<GroupMember> members = groupService.getGroupMembers(groupId);
            
            return new ResponseEntity<>(
                new ApiResponse("Group members retrieved successfully", true, members),
                HttpStatus.OK
            );
            
        } catch (Exception e) {
            return new ResponseEntity<>(
                new ApiResponse("Failed to get group members: " + e.getMessage(), false),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }
    
    @GetMapping("/{groupId}/admins")
    public ResponseEntity<ApiResponse> getGroupAdmins(@PathVariable UUID groupId) {
        try {
            List<GroupMember> admins = groupService.getGroupAdmins(groupId);
            
            return new ResponseEntity<>(
                new ApiResponse("Group admins retrieved successfully", true, admins),
                HttpStatus.OK
            );
            
        } catch (Exception e) {
            return new ResponseEntity<>(
                new ApiResponse("Failed to get group admins: " + e.getMessage(), false),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }
    
    @GetMapping("/{groupId}/moderators")
    public ResponseEntity<ApiResponse> getGroupModerators(@PathVariable UUID groupId) {
        try {
            List<GroupMember> moderators = groupService.getGroupModerators(groupId);
            
            return new ResponseEntity<>(
                new ApiResponse("Group moderators retrieved successfully", true, moderators),
                HttpStatus.OK
            );
            
        } catch (Exception e) {
            return new ResponseEntity<>(
                new ApiResponse("Failed to get group moderators: " + e.getMessage(), false),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }
    
    // Group discovery
    @GetMapping("/public")
    public ResponseEntity<ApiResponse> getPublicGroups() {
        try {
            List<Group> groups = groupService.getPublicGroups();
            
            return new ResponseEntity<>(
                new ApiResponse("Public groups retrieved successfully", true, groups),
                HttpStatus.OK
            );
            
        } catch (Exception e) {
            return new ResponseEntity<>(
                new ApiResponse("Failed to get public groups: " + e.getMessage(), false),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }
    
    @GetMapping("/public/paginated")
    public ResponseEntity<ApiResponse> getPublicGroupsPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            PaginatedResponse<Group> groups = groupService.getPublicGroupsPaginated(page, size);
            
            return new ResponseEntity<>(
                new ApiResponse("Public groups retrieved successfully", true, groups),
                HttpStatus.OK
            );
            
        } catch (Exception e) {
            return new ResponseEntity<>(
                new ApiResponse("Failed to get public groups: " + e.getMessage(), false),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }
    
    @GetMapping("/search")
    public ResponseEntity<ApiResponse> searchGroups(@RequestParam String query) {
        try {
            List<Group> groups = groupService.searchGroups(query);
            
            return new ResponseEntity<>(
                new ApiResponse("Groups search completed successfully", true, groups),
                HttpStatus.OK
            );
            
        } catch (Exception e) {
            return new ResponseEntity<>(
                new ApiResponse("Failed to search groups: " + e.getMessage(), false),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }
    
    @GetMapping("/search/paginated")
    public ResponseEntity<ApiResponse> searchGroupsPaginated(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            PaginatedResponse<Group> groups = groupService.searchGroupsPaginated(query, page, size);
            
            return new ResponseEntity<>(
                new ApiResponse("Groups search completed successfully", true, groups),
                HttpStatus.OK
            );
            
        } catch (Exception e) {
            return new ResponseEntity<>(
                new ApiResponse("Failed to search groups: " + e.getMessage(), false),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }
    
    @GetMapping("/joinable")
    public ResponseEntity<ApiResponse> getJoinableGroups(
            @RequestHeader("Authorization") String jwt) {
        try {
            User user = userService.getUserFromToken(jwt);
            List<Group> groups = groupService.getJoinableGroupsForUser(user.getId());
            
            return new ResponseEntity<>(
                new ApiResponse("Joinable groups retrieved successfully", true, groups),
                HttpStatus.OK
            );
            
        } catch (Exception e) {
            return new ResponseEntity<>(
                new ApiResponse("Failed to get joinable groups: " + e.getMessage(), false),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }
    
    @GetMapping("/joinable/paginated")
    public ResponseEntity<ApiResponse> getJoinableGroupsPaginated(
            @RequestHeader("Authorization") String jwt,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            User user = userService.getUserFromToken(jwt);
            PaginatedResponse<Group> groups = groupService.getJoinableGroupsForUserPaginated(user.getId(), page, size);
            
            return new ResponseEntity<>(
                new ApiResponse("Joinable groups retrieved successfully", true, groups),
                HttpStatus.OK
            );
            
        } catch (Exception e) {
            return new ResponseEntity<>(
                new ApiResponse("Failed to get joinable groups: " + e.getMessage(), false),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }
    
    // Group settings
    @PutMapping("/{groupId}/settings")
    public ResponseEntity<ApiResponse> updateGroupSettings(
            @RequestHeader("Authorization") String jwt,
            @PathVariable UUID groupId,
            @RequestBody GroupSettingsRequest request) {
        try {
            User admin = userService.getUserFromToken(jwt);
            
            Group group = groupService.updateGroupSettings(
                groupId,
                admin,
                request.getIsPublic(),
                request.getAllowMemberInvites(),
                request.getMaxMembers()
            );
            
            return new ResponseEntity<>(
                new ApiResponse("Group settings updated successfully", true, group),
                HttpStatus.OK
            );
            
        } catch (Exception e) {
            return new ResponseEntity<>(
                new ApiResponse("Failed to update group settings: " + e.getMessage(), false),
                HttpStatus.BAD_REQUEST
            );
        }
    }
    
    @PutMapping("/{groupId}/image")
    public ResponseEntity<ApiResponse> updateGroupImage(
            @RequestHeader("Authorization") String jwt,
            @PathVariable UUID groupId,
            @RequestBody Map<String, String> request) {
        try {
            User admin = userService.getUserFromToken(jwt);
            String imageUrl = request.get("imageUrl");
            
            if (imageUrl == null) {
                return new ResponseEntity<>(
                    new ApiResponse("Image URL is required", false),
                    HttpStatus.BAD_REQUEST
                );
            }
            
            Group group = groupService.updateGroupImage(groupId, admin, imageUrl);
            
            return new ResponseEntity<>(
                new ApiResponse("Group image updated successfully", true, group),
                HttpStatus.OK
            );
            
        } catch (Exception e) {
            return new ResponseEntity<>(
                new ApiResponse("Failed to update group image: " + e.getMessage(), false),
                HttpStatus.BAD_REQUEST
            );
        }
    }
    
    @PutMapping("/{groupId}/cover-image")
    public ResponseEntity<ApiResponse> updateGroupCoverImage(
            @RequestHeader("Authorization") String jwt,
            @PathVariable UUID groupId,
            @RequestBody Map<String, String> request) {
        try {
            User admin = userService.getUserFromToken(jwt);
            String coverImageUrl = request.get("coverImageUrl");
            
            if (coverImageUrl == null) {
                return new ResponseEntity<>(
                    new ApiResponse("Cover image URL is required", false),
                    HttpStatus.BAD_REQUEST
                );
            }
            
            Group group = groupService.updateGroupCoverImage(groupId, admin, coverImageUrl);
            
            return new ResponseEntity<>(
                new ApiResponse("Group cover image updated successfully", true, group),
                HttpStatus.OK
            );
            
        } catch (Exception e) {
            return new ResponseEntity<>(
                new ApiResponse("Failed to update group cover image: " + e.getMessage(), false),
                HttpStatus.BAD_REQUEST
            );
        }
    }
    
    // Group statistics
    @GetMapping("/{groupId}/stats")
    public ResponseEntity<ApiResponse> getGroupStatistics(@PathVariable UUID groupId) {
        try {
            Group.GroupStats stats = groupService.getGroupStatistics(groupId);
            
            return new ResponseEntity<>(
                new ApiResponse("Group statistics retrieved successfully", true, stats),
                HttpStatus.OK
            );
            
        } catch (Exception e) {
            return new ResponseEntity<>(
                new ApiResponse("Failed to get group statistics: " + e.getMessage(), false),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }
    
    // DTOs
    public static class CreateGroupRequest {
        private String name;
        private String description;
        private Group.GroupType groupType;
        private Boolean isPublic;
        private List<UUID> memberIds;
        
        // Constructors, getters, setters
        public CreateGroupRequest() {}
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public Group.GroupType getGroupType() { return groupType; }
        public void setGroupType(Group.GroupType groupType) { this.groupType = groupType; }
        
        public Boolean getIsPublic() { return isPublic; }
        public void setIsPublic(Boolean isPublic) { this.isPublic = isPublic; }
        
        public List<UUID> getMemberIds() { return memberIds; }
        public void setMemberIds(List<UUID> memberIds) { this.memberIds = memberIds; }
    }
    
    public static class UpdateGroupRequest {
        private String name;
        private String description;
        private String groupImage;
        private String groupCoverImage;
        
        // Constructors, getters, setters
        public UpdateGroupRequest() {}
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public String getGroupImage() { return groupImage; }
        public void setGroupImage(String groupImage) { this.groupImage = groupImage; }
        
        public String getGroupCoverImage() { return groupCoverImage; }
        public void setGroupCoverImage(String groupCoverImage) { this.groupCoverImage = groupCoverImage; }
    }
    
    public static class GroupSettingsRequest {
        private Boolean isPublic;
        private Boolean allowMemberInvites;
        private Integer maxMembers;
        
        // Constructors, getters, setters
        public GroupSettingsRequest() {}
        
        public Boolean getIsPublic() { return isPublic; }
        public void setIsPublic(Boolean isPublic) { this.isPublic = isPublic; }
        
        public Boolean getAllowMemberInvites() { return allowMemberInvites; }
        public void setAllowMemberInvites(Boolean allowMemberInvites) { this.allowMemberInvites = allowMemberInvites; }
        
        public Integer getMaxMembers() { return maxMembers; }
        public void setMaxMembers(Integer maxMembers) { this.maxMembers = maxMembers; }
    }
}

