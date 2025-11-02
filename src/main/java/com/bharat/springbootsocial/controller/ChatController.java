package com.bharat.springbootsocial.controller;

import com.bharat.springbootsocial.entity.Chat;
import com.bharat.springbootsocial.entity.Message;
import com.bharat.springbootsocial.entity.User;
import java.util.UUID;
import com.bharat.springbootsocial.exception.ChatException;
import com.bharat.springbootsocial.request.CreateChatRequest;
import com.bharat.springbootsocial.request.EnhancedMessageRequest;
import com.bharat.springbootsocial.request.MessageReactionRequest;
import com.bharat.springbootsocial.request.MessageReadRequest;
import com.bharat.springbootsocial.response.EnhancedMessageResponse;
import com.bharat.springbootsocial.response.MessageReactionResponse;
import com.bharat.springbootsocial.response.MessageReadResponse;
import com.bharat.springbootsocial.services.ChatService;
import com.bharat.springbootsocial.services.ServiceInt;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chats")
@AllArgsConstructor
public class ChatController {
    @Autowired
    private ChatService chatService;
    @Autowired
    private ServiceInt userService;

    @PostMapping()
    public Chat createChat(@RequestHeader("Authorization") String token,@RequestBody CreateChatRequest req){
        User reqUser = userService.getUserFromToken(token);
        User user2 = userService.getUserById(req.getUserId());
        return chatService.createChat(reqUser, user2);
    }

    @GetMapping()
    public List<Chat> findUsersChats(@RequestHeader("Authorization") String token){
        User user = userService.getUserFromToken(token);
        return chatService.getChatsByUserId(user.getId());
    }

    @GetMapping("{chatId}")
    public Chat findChatById(@PathVariable UUID chatId) throws ChatException {
        return chatService.getChatById(chatId);
    }
    
    @DeleteMapping("{chatId}")
    public ResponseEntity<Void> deleteChat(
            @RequestHeader("Authorization") String token,
            @PathVariable UUID chatId) throws ChatException {
        User user = userService.getUserFromToken(token);
        chatService.deleteChat(chatId, user);
        return ResponseEntity.ok().build();
    }
    
    // Enhanced message endpoints
    @PostMapping("/messages")
    public ResponseEntity<EnhancedMessageResponse> sendMessage(
            @RequestHeader("Authorization") String token,
            @RequestBody EnhancedMessageRequest request) throws ChatException {
        User user = userService.getUserFromToken(token);
        EnhancedMessageResponse response = chatService.sendMessage(request, user);
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/messages/{messageId}")
    public ResponseEntity<EnhancedMessageResponse> editMessage(
            @RequestHeader("Authorization") String token,
            @PathVariable UUID messageId,
            @RequestBody String newContent) throws ChatException {
        User user = userService.getUserFromToken(token);
        EnhancedMessageResponse response = chatService.editMessage(messageId, newContent, user);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/messages/{messageId}")
    public ResponseEntity<Void> deleteMessage(
            @RequestHeader("Authorization") String token,
            @PathVariable UUID messageId) throws ChatException {
        User user = userService.getUserFromToken(token);
        chatService.deleteMessage(messageId, user);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/{chatId}/messages")
    public ResponseEntity<List<EnhancedMessageResponse>> getChatMessages(
            @RequestHeader("Authorization") String token,
            @PathVariable UUID chatId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) throws ChatException {
        User user = userService.getUserFromToken(token);
        List<EnhancedMessageResponse> messages = chatService.getChatMessages(chatId, user, page, size);
        return ResponseEntity.ok(messages);
    }
    
    // Message reaction endpoints
    @PostMapping("/messages/reactions")
    public ResponseEntity<MessageReactionResponse> addReaction(
            @RequestHeader("Authorization") String token,
            @RequestBody MessageReactionRequest request) throws ChatException {
        User user = userService.getUserFromToken(token);
        MessageReactionResponse response = chatService.addReaction(request, user);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/messages/{messageId}/reactions")
    public ResponseEntity<Void> removeReaction(
            @RequestHeader("Authorization") String token,
            @PathVariable UUID messageId) throws ChatException {
        User user = userService.getUserFromToken(token);
        chatService.removeReaction(messageId, user);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/messages/{messageId}/reactions")
    public ResponseEntity<List<MessageReactionResponse>> getMessageReactions(
            @PathVariable UUID messageId) throws ChatException {
        List<MessageReactionResponse> reactions = chatService.getMessageReactions(messageId);
        return ResponseEntity.ok(reactions);
    }
    
    // Message read endpoints
    @PostMapping("/messages/read")
    public ResponseEntity<MessageReadResponse> markAsRead(
            @RequestHeader("Authorization") String token,
            @RequestBody MessageReadRequest request) throws ChatException {
        User user = userService.getUserFromToken(token);
        MessageReadResponse response = chatService.markAsRead(request, user);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/messages/{messageId}/reads")
    public ResponseEntity<List<MessageReadResponse>> getMessageReads(
            @PathVariable UUID messageId) throws ChatException {
        List<MessageReadResponse> reads = chatService.getMessageReads(messageId);
        return ResponseEntity.ok(reads);
    }
    
    @GetMapping("/{chatId}/unread-count")
    public ResponseEntity<Long> getUnreadMessageCount(
            @RequestHeader("Authorization") String token,
            @PathVariable UUID chatId) throws ChatException {
        User user = userService.getUserFromToken(token);
        long count = chatService.getUnreadMessageCount(chatId, user);
        return ResponseEntity.ok(count);
    }
    
    @GetMapping("/{chatId}/unread-messages")
    public ResponseEntity<List<Message>> getUnreadMessages(
            @RequestHeader("Authorization") String token,
            @PathVariable UUID chatId) throws ChatException {
        User user = userService.getUserFromToken(token);
        List<Message> messages = chatService.getUnreadMessages(chatId, user);
        return ResponseEntity.ok(messages);
    }
}
