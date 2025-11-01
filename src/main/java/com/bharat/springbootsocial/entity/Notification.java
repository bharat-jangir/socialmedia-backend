package com.bharat.springbootsocial.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "notifications")
@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false, columnDefinition = "BINARY(16)")
    private User recipient;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", columnDefinition = "BINARY(16)")
    private User sender;
    
    @Column(name = "type", length = 50)
    @Enumerated(EnumType.STRING)
    private NotificationType type;
    
    @Column(name = "title", length = 200)
    private String title;
    
    @Column(name = "message", length = 500)
    private String message;
    
    @Column(name = "related_entity_type", length = 50)
    private String relatedEntityType; // "POST", "STORY", "COMMENT", "LIKE", etc.
    
    @Column(name = "related_entity_id")
    private UUID relatedEntityId;
    
    @Column(name = "is_read")
    private Boolean isRead = false;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "read_at")
    private LocalDateTime readAt;
    
    public enum NotificationType {
        LIKE_POST,
        LIKE_STORY,
        LIKE_COMMENT,
        COMMENT_POST,
        COMMENT_STORY,
        FOLLOW,
        STORY_VIEW,
        MENTION,
        CALL_INVITATION,
        CALL_RESPONSE,
        GROUP_INVITATION,
        SYSTEM
    }
}
