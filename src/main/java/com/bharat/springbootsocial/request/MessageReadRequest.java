package com.bharat.springbootsocial.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageReadRequest {
    
    private UUID messageId;
    
    // Optional: if not provided, will be extracted from JWT token
    private UUID userId;
}
