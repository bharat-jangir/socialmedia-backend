package com.bharat.springbootsocial.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "user_groups")
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Group {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(length = 500)
    private String description;
    
    @Column(name = "group_image")
    private String groupImage;
    
    @Column(name = "group_cover_image")
    private String groupCoverImage;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User createdBy;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User admin;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private GroupType groupType;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private GroupStatus status;
    
    @Column(name = "max_members")
    private Integer maxMembers = 100; // Default max members
    
    @Column(name = "is_public")
    private Boolean isPublic = false; // Default to private group
    
    @Column(name = "allow_member_invites")
    private Boolean allowMemberInvites = true;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "last_activity")
    private LocalDateTime lastActivity;
    
    // Group members (many-to-many with additional info)
    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<GroupMember> members = new ArrayList<>();
    
    // Group messages
    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<GroupMessage> messages = new ArrayList<>();
    
    // Group call rooms
    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<GroupCallRoom> callRooms = new ArrayList<>();
    
    public enum GroupType {
        FRIENDS,           // Friend group
        FAMILY,            // Family group
        WORK,              // Work group
        STUDY,             // Study group
        HOBBY,             // Hobby group
        GENERAL            // General group
    }
    
    public enum GroupStatus {
        ACTIVE,            // Group is active
        INACTIVE,          // Group is inactive
        ARCHIVED,          // Group is archived
        DELETED            // Group is deleted
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        lastActivity = LocalDateTime.now();
        if (status == null) {
            status = GroupStatus.ACTIVE;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public void updateLastActivity() {
        this.lastActivity = LocalDateTime.now();
    }
    
    public boolean isMember(User user) {
        return members.stream()
                .anyMatch(member -> member.getUser().getId().equals(user.getId()) && 
                         member.getStatus() == GroupMember.MemberStatus.ACTIVE);
    }
    
    public boolean isAdmin(User user) {
        return admin.getId().equals(user.getId());
    }
    
    public boolean isCreator(User user) {
        return createdBy.getId().equals(user.getId());
    }
    
    public boolean canUserJoin(User user) {
        if (!isPublic && !isMember(user)) {
            return false;
        }
        return status == GroupStatus.ACTIVE && members.size() < maxMembers;
    }
    
    public int getActiveMemberCount() {
        return (int) members.stream()
                .filter(member -> member.getStatus() == GroupMember.MemberStatus.ACTIVE)
                .count();
    }
    
    // DTO for group statistics
    public static class GroupStats {
        private UUID groupId;
        private String groupName;
        private int totalMembers;
        private int activeMembers;
        private int totalMessages;
        private int totalCallRooms;
        private int activeCallRooms;
        private LocalDateTime lastActivity;
        private GroupStatus status;
        
        // Constructors
        public GroupStats() {}
        
        public GroupStats(UUID groupId, String groupName, int totalMembers, int activeMembers, 
                         int totalMessages, int totalCallRooms, int activeCallRooms, 
                         LocalDateTime lastActivity, GroupStatus status) {
            this.groupId = groupId;
            this.groupName = groupName;
            this.totalMembers = totalMembers;
            this.activeMembers = activeMembers;
            this.totalMessages = totalMessages;
            this.totalCallRooms = totalCallRooms;
            this.activeCallRooms = activeCallRooms;
            this.lastActivity = lastActivity;
            this.status = status;
        }
        
        // Getters and setters
        public UUID getGroupId() { return groupId; }
        public void setGroupId(UUID groupId) { this.groupId = groupId; }
        
        public String getGroupName() { return groupName; }
        public void setGroupName(String groupName) { this.groupName = groupName; }
        
        public int getTotalMembers() { return totalMembers; }
        public void setTotalMembers(int totalMembers) { this.totalMembers = totalMembers; }
        
        public int getActiveMembers() { return activeMembers; }
        public void setActiveMembers(int activeMembers) { this.activeMembers = activeMembers; }
        
        public int getTotalMessages() { return totalMessages; }
        public void setTotalMessages(int totalMessages) { this.totalMessages = totalMessages; }
        
        public int getTotalCallRooms() { return totalCallRooms; }
        public void setTotalCallRooms(int totalCallRooms) { this.totalCallRooms = totalCallRooms; }
        
        public int getActiveCallRooms() { return activeCallRooms; }
        public void setActiveCallRooms(int activeCallRooms) { this.activeCallRooms = activeCallRooms; }
        
        public LocalDateTime getLastActivity() { return lastActivity; }
        public void setLastActivity(LocalDateTime lastActivity) { this.lastActivity = lastActivity; }
        
        public GroupStatus getStatus() { return status; }
        public void setStatus(GroupStatus status) { this.status = status; }
    }
}
