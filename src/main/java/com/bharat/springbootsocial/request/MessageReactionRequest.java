package com.bharat.springbootsocial.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageReactionRequest {
    
    private UUID messageId;
    private String emoji;
    
    // Optional: if not provided, will be extracted from JWT token
    private UUID userId;
}
