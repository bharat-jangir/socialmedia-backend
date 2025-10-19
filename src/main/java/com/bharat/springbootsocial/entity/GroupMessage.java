package com.bharat.springbootsocial.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "user_group_messages")
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class GroupMessage {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(nullable = false, length = 2000)
    private String content;
    
    @Column(name = "message_type", length = 20)
    @Enumerated(EnumType.STRING)
    private MessageType messageType;
    
    @Column(name = "image_url")
    private String imageUrl;
    
    @Column(name = "video_url")
    private String videoUrl;
    
    @Column(name = "file_url")
    private String fileUrl;
    
    @Column(name = "file_name")
    private String fileName;
    
    @Column(name = "file_size")
    private Long fileSize;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User sender;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    @JsonIgnore
    private Group group;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reply_to_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private GroupMessage replyTo;
    
    @Column(name = "is_edited")
    private Boolean isEdited = false;
    
    @Column(name = "edited_at")
    private LocalDateTime editedAt;
    
    @Column(name = "is_deleted")
    private Boolean isDeleted = false;
    
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Message reactions
    @OneToMany(mappedBy = "message", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<GroupMessageReaction> reactions = new ArrayList<>();
    
    // Message read status
    @OneToMany(mappedBy = "message", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<GroupMessageRead> readBy = new ArrayList<>();
    
    public enum MessageType {
        TEXT,              // Text message
        IMAGE,             // Image message
        VIDEO,             // Video message
        FILE,              // File message
        AUDIO,             // Audio message
        SYSTEM,            // System message
        CALL_START,        // Call started
        CALL_END,          // Call ended
        MEMBER_JOINED,     // Member joined
        MEMBER_LEFT,       // Member left
        GROUP_CREATED,     // Group created
        GROUP_UPDATED      // Group updated
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (messageType == null) {
            messageType = MessageType.TEXT;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public void editMessage(String newContent) {
        this.content = newContent;
        this.isEdited = true;
        this.editedAt = LocalDateTime.now();
    }
    
    public void deleteMessage() {
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
        this.content = "This message was deleted";
    }
    
    public boolean isSystemMessage() {
        return messageType == MessageType.SYSTEM || 
               messageType == MessageType.CALL_START ||
               messageType == MessageType.CALL_END ||
               messageType == MessageType.MEMBER_JOINED ||
               messageType == MessageType.MEMBER_LEFT ||
               messageType == MessageType.GROUP_CREATED ||
               messageType == MessageType.GROUP_UPDATED;
    }
    
    public int getReactionCount() {
        return reactions.size();
    }
    
    public int getReadCount() {
        return readBy.size();
    }
}
