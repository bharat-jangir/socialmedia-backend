package com.bharat.springbootsocial.request;

import com.bharat.springbootsocial.entity.Message;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EnhancedMessageRequest {
    
    private String content;
    private String imageUrl;
    private String videoUrl;
    private String fileUrl;
    private String fileName;
    private Long fileSize;
    private Message.MessageType messageType;
    private UUID chatId;
    private UUID replyToId; // For replying to a message
    
    // Optional: if not provided, will be extracted from JWT token
    private UUID userId;
}
