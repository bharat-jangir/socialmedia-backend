package com.bharat.springbootsocial.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_group_members")
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class GroupMember {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Group group;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User user;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MemberRole role;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MemberStatus status;
    
    @Column(name = "joined_at")
    private LocalDateTime joinedAt;
    
    @Column(name = "left_at")
    private LocalDateTime leftAt;
    
    @Column(name = "last_seen")
    private LocalDateTime lastSeen;
    
    @Column(name = "is_muted")
    private Boolean isMuted = false;
    
    @Column(name = "is_pinned")
    private Boolean isPinned = false;
    
    @Column(name = "nickname")
    private String nickname;
    
    public enum MemberRole {
        ADMIN,             // Group admin
        MODERATOR,         // Group moderator
        MEMBER             // Regular member
    }
    
    public enum MemberStatus {
        ACTIVE,            // Active member
        INACTIVE,          // Inactive member
        LEFT,              // Left the group
        REMOVED,           // Removed from group
        PENDING            // Pending approval
    }
    
    @PrePersist
    protected void onCreate() {
        joinedAt = LocalDateTime.now();
        lastSeen = LocalDateTime.now();
        if (status == null) {
            status = MemberStatus.ACTIVE;
        }
        if (role == null) {
            role = MemberRole.MEMBER;
        }
    }
    
    public void leaveGroup() {
        this.status = MemberStatus.LEFT;
        this.leftAt = LocalDateTime.now();
    }
    
    public void removeFromGroup() {
        this.status = MemberStatus.REMOVED;
        this.leftAt = LocalDateTime.now();
    }
    
    public void updateLastSeen() {
        this.lastSeen = LocalDateTime.now();
    }
    
    public boolean isActive() {
        return status == MemberStatus.ACTIVE;
    }
    
    public boolean isAdmin() {
        return role == MemberRole.ADMIN;
    }
    
    public boolean isModerator() {
        return role == MemberRole.MODERATOR || role == MemberRole.ADMIN;
    }
}
