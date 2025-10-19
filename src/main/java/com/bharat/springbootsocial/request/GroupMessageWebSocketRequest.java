package com.bharat.springbootsocial.request;

import com.bharat.springbootsocial.entity.GroupMessage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupMessageWebSocketRequest {
    private UUID groupId;
    private String content;
    private GroupMessage.MessageType messageType;
    private String mediaUrl;
    private String mediaType;
    private UUID replyToMessageId;
    private String reaction;
    private UUID messageId;
    private String action; // "send", "react", "delete", "edit", "typing", "stop_typing"
}
