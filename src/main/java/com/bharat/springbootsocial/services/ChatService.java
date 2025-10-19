package com.bharat.springbootsocial.services;

import com.bharat.springbootsocial.entity.Chat;
import com.bharat.springbootsocial.entity.Message;
import com.bharat.springbootsocial.entity.MessageReaction;
import com.bharat.springbootsocial.entity.MessageRead;
import com.bharat.springbootsocial.entity.User;
import com.bharat.springbootsocial.exception.ChatException;
import com.bharat.springbootsocial.request.EnhancedMessageRequest;
import com.bharat.springbootsocial.request.MessageReactionRequest;
import com.bharat.springbootsocial.request.MessageReadRequest;
import com.bharat.springbootsocial.response.EnhancedMessageResponse;
import com.bharat.springbootsocial.response.MessageReactionResponse;
import com.bharat.springbootsocial.response.MessageReadResponse;

import java.util.List;
import java.util.UUID;

public interface ChatService {
    Chat createChat(User reqUser, User user2);
    Chat getChatById(UUID chatId) throws ChatException;
    List<Chat> getChatsByUserId(UUID userId);
    
    // Enhanced message operations
    EnhancedMessageResponse sendMessage(EnhancedMessageRequest request, User user) throws ChatException;
    EnhancedMessageResponse editMessage(UUID messageId, String newContent, User user) throws ChatException;
    void deleteMessage(UUID messageId, User user) throws ChatException;
    List<EnhancedMessageResponse> getChatMessages(UUID chatId, User user, int page, int size) throws ChatException;
    
    // Message reactions
    MessageReactionResponse addReaction(MessageReactionRequest request, User user) throws ChatException;
    void removeReaction(UUID messageId, User user) throws ChatException;
    List<MessageReactionResponse> getMessageReactions(UUID messageId) throws ChatException;
    
    // Message reads
    MessageReadResponse markAsRead(MessageReadRequest request, User user) throws ChatException;
    List<MessageReadResponse> getMessageReads(UUID messageId) throws ChatException;
    long getUnreadMessageCount(UUID chatId, User user) throws ChatException;
    List<Message> getUnreadMessages(UUID chatId, User user) throws ChatException;
}
