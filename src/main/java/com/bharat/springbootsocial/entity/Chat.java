package com.bharat.springbootsocial.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "chats")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Chat {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;
    private String chat_name;
    private String chat_image;
    // each chat can have multiple users and each user can be in multiple chats
    @ManyToMany
    @JoinTable(
        name = "chats_users",
        joinColumns = @JoinColumn(name = "chats_id", columnDefinition = "BINARY(16)"),
        inverseJoinColumns = @JoinColumn(name = "users_id", columnDefinition = "BINARY(16)")
    )
    private List<User> users = new ArrayList<>();
    @OneToMany(mappedBy = "chat")
    private List<Message> messages = new ArrayList<>();
    private LocalDateTime timestamp;
}
