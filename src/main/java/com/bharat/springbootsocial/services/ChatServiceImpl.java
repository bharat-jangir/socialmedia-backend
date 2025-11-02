package com.bharat.springbootsocial.services;

import com.bharat.springbootsocial.entity.Chat;
import com.bharat.springbootsocial.entity.Message;
import com.bharat.springbootsocial.entity.MessageReaction;
import com.bharat.springbootsocial.entity.MessageRead;
import com.bharat.springbootsocial.entity.User;
import com.bharat.springbootsocial.exception.ChatException;
import com.bharat.springbootsocial.repository.ChatRepo;
import com.bharat.springbootsocial.repository.MessageReactionRepo;
import com.bharat.springbootsocial.repository.MessageReadRepo;
import com.bharat.springbootsocial.repository.MessageRepo;
import com.bharat.springbootsocial.repository.UserRepo;
import com.bharat.springbootsocial.request.EnhancedMessageRequest;
import com.bharat.springbootsocial.request.MessageReactionRequest;
import com.bharat.springbootsocial.request.MessageReadRequest;
import com.bharat.springbootsocial.response.EnhancedMessageResponse;
import com.bharat.springbootsocial.response.MessageReactionResponse;
import com.bharat.springbootsocial.response.MessageReadResponse;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ChatServiceImpl implements ChatService{
    
    @Autowired
    private ChatRepo chatRepo;
    
    @Autowired
    private MessageRepo messageRepo;
    
    @Autowired
    private MessageReactionRepo messageReactionRepo;
    
    @Autowired
    private MessageReadRepo messageReadRepo;
    
    @Autowired
    private UserRepo userRepo;
    
    @Override
    public Chat createChat(User reqUser, User user2) {
        Chat isExisting = chatRepo.findChatByUsersId(reqUser, user2);
        if(isExisting != null)
            return isExisting;

        Chat chat = new Chat();
        chat.getUsers().add(user2);
        chat.getUsers().add(reqUser);
        // Use UTC to ensure consistent timezone handling
        chat.setTimestamp(LocalDateTime.now(ZoneOffset.UTC));

        return chatRepo.save(chat);
    }

    @Override
    public Chat getChatById(UUID chatId) throws ChatException {
        Optional<Chat> chatOptional = chatRepo.findById(chatId);
        if(chatOptional.isPresent())
            return chatOptional.get();
        else
            throw new ChatException("Chat not found it with id "+chatId);
    }
    
    /**
     * Get chat with users eagerly loaded (to avoid LazyInitializationException)
     */
    public Chat getChatByIdWithUsers(UUID chatId) throws ChatException {
        Optional<Chat> chatOptional = chatRepo.findByIdWithUsers(chatId);
        if(chatOptional.isPresent())
            return chatOptional.get();
        else
            throw new ChatException("Chat not found it with id "+chatId);
    }

    @Override
    public List<Chat> getChatsByUserId(UUID userId) {
        return chatRepo.findByUsersId(userId);
    }
    
    @Override
    @Transactional
    public void deleteChat(UUID chatId, User user) throws ChatException {
        Chat chat = getChatByIdWithUsers(chatId);
        
        // Check if user is part of the chat
        boolean userIsInChat = chat.getUsers().stream()
                .anyMatch(chatUser -> chatUser.getId().equals(user.getId()));
        
        if (!userIsInChat) {
            throw new ChatException("User is not part of this chat");
        }
        
        // Delete all messages in the chat first
        List<Message> messages = messageRepo.findByChatId(chatId);
        if (messages != null && !messages.isEmpty()) {
            messageRepo.deleteAll(messages);
        }
        
        // Delete the chat
        chatRepo.delete(chat);
    }
    
    // Enhanced message operations
    @Override
    @Transactional
    public EnhancedMessageResponse sendMessage(EnhancedMessageRequest request, User user) throws ChatException {
        System.out.println("=== ChatServiceImpl.sendMessage() CALLED ===");
        System.out.println("  Request chatId: " + request.getChatId());
        System.out.println("  Request content: " + request.getContent());
        System.out.println("  User ID: " + user.getId());
        
        Chat chat = getChatById(request.getChatId());
        System.out.println("  Chat found: " + chat.getId());
        System.out.println("  Chat users count: " + chat.getUsers().size());
        
        // Check if user is part of the chat by comparing IDs (more reliable than contains())
        UUID userId = user.getId();
        boolean userIsInChat = chat.getUsers().stream()
                .anyMatch(chatUser -> chatUser.getId().equals(userId));
        
        System.out.println("  Checking user membership:");
        System.out.println("    Current user ID: " + userId);
        System.out.println("    Chat user IDs: " + chat.getUsers().stream()
                .map(u -> u.getId().toString())
                .collect(java.util.stream.Collectors.joining(", ")));
        System.out.println("    User is in chat: " + userIsInChat);
        
        if (!userIsInChat) {
            System.err.println("❌ User is not part of this chat");
            throw new ChatException("User is not part of this chat");
        }
        System.out.println("  ✓ User is part of the chat");
        
        System.out.println("Creating Message entity...");
        Message message = new Message();
        message.setContent(request.getContent());
        message.setImage(request.getImageUrl());
        message.setVideoUrl(request.getVideoUrl());
        message.setFileUrl(request.getFileUrl());
        message.setFileName(request.getFileName());
        message.setFileSize(request.getFileSize());
        message.setMessageType(request.getMessageType() != null ? request.getMessageType() : Message.MessageType.TEXT);
        message.setUser(user);
        message.setChat(chat);
        
        System.out.println("  Message content set: " + message.getContent());
        System.out.println("  Message user set: " + message.getUser().getId());
        System.out.println("  Message chat set: " + message.getChat().getId());
        
        // Handle reply to message
        if (request.getReplyToId() != null) {
            Optional<Message> replyToMessage = messageRepo.findById(request.getReplyToId());
            if (replyToMessage.isPresent()) {
                message.setReplyTo(replyToMessage.get());
            }
        }
        
        System.out.println("Saving message to database using messageRepo.saveAndFlush()...");
        try {
            // Use saveAndFlush to ensure immediate database write
            Message savedMessage = messageRepo.saveAndFlush(message);
            System.out.println("✓ Message entity saved and flushed, ID: " + (savedMessage.getId() != null ? savedMessage.getId() : "NULL"));
            
            // Verify the message was saved by checking it exists
            if (savedMessage.getId() != null) {
                Optional<Message> verifyMessage = messageRepo.findById(savedMessage.getId());
                if (verifyMessage.isPresent()) {
                    System.out.println("✓✓✓ VERIFIED: Message exists in database ✓✓✓");
                    Message verified = verifyMessage.get();
                    System.out.println("  Verified message content: " + verified.getContent());
                    System.out.println("  Verified message chat ID: " + verified.getChat().getId());
                    System.out.println("  Verified message user ID: " + verified.getUser().getId());
                } else {
                    System.err.println("❌ WARNING: Message saved but not found when verifying!");
                }
            } else {
                System.err.println("❌ ERROR: Saved message has NULL ID!");
            }
            
            System.out.println("✓✓✓ MESSAGE SAVED TO DATABASE IN REPOSITORY ✓✓✓");
            System.out.println("  Saved message ID: " + savedMessage.getId());
            System.out.println("  Saved message content: " + savedMessage.getContent());
            System.out.println("  Saved message timestamp: " + savedMessage.getTimestamp());
            System.out.println("  Saved message chat ID: " + savedMessage.getChat().getId());
            System.out.println("  Saved message user ID: " + savedMessage.getUser().getId());
            
            EnhancedMessageResponse response = EnhancedMessageResponse.fromEntity(savedMessage);
            System.out.println("  Response created with ID: " + response.getId());
            
            return response;
        } catch (Exception e) {
            System.err.println("❌❌❌ EXCEPTION DURING messageRepo.saveAndFlush() ❌❌❌");
            System.err.println("Exception type: " + e.getClass().getName());
            System.err.println("Exception message: " + e.getMessage());
            System.err.println("Stack trace:");
            e.printStackTrace();
            throw e;
        }
    }
    
    @Override
    @Transactional
    public EnhancedMessageResponse editMessage(UUID messageId, String newContent, User user) throws ChatException {
        Optional<Message> messageOptional = messageRepo.findById(messageId);
        if (messageOptional.isEmpty()) {
            throw new ChatException("Message not found with id: " + messageId);
        }
        
        Message message = messageOptional.get();
        
        // Check if user is the sender
        if (!message.getUser().getId().equals(user.getId())) {
            throw new ChatException("You can only edit your own messages");
        }
        
        // Check if message is not deleted
        if (message.getIsDeleted()) {
            throw new ChatException("Cannot edit deleted message");
        }
        
        message.editMessage(newContent);
        Message savedMessage = messageRepo.save(message);
        return EnhancedMessageResponse.fromEntity(savedMessage);
    }
    
    @Override
    @Transactional
    public void deleteMessage(UUID messageId, User user) throws ChatException {
        Optional<Message> messageOptional = messageRepo.findById(messageId);
        if (messageOptional.isEmpty()) {
            throw new ChatException("Message not found with id: " + messageId);
        }
        
        Message message = messageOptional.get();
        
        // Check if user is the sender
        if (!message.getUser().getId().equals(user.getId())) {
            throw new ChatException("You can only delete your own messages");
        }
        
        message.deleteMessage();
        messageRepo.save(message);
    }
    
    @Override
    public List<EnhancedMessageResponse> getChatMessages(UUID chatId, User user, int page, int size) throws ChatException {
        Chat chat = getChatById(chatId);
        
        // Check if user is part of the chat
        if (!chat.getUsers().contains(user)) {
            throw new ChatException("User is not part of this chat");
        }
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Message> messagePage = messageRepo.findByChatIdOrderByTimestampDesc(chatId, pageable);
        
        return messagePage.getContent().stream()
                .map(EnhancedMessageResponse::fromEntity)
                .collect(Collectors.toList());
    }
    
    // Message reactions
    @Override
    @Transactional
    public MessageReactionResponse addReaction(MessageReactionRequest request, User user) throws ChatException {
        Optional<Message> messageOptional = messageRepo.findById(request.getMessageId());
        if (messageOptional.isEmpty()) {
            throw new ChatException("Message not found with id: " + request.getMessageId());
        }
        
        Message message = messageOptional.get();
        
        // Check if user is part of the chat
        if (!message.getChat().getUsers().contains(user)) {
            throw new ChatException("User is not part of this chat");
        }
        
        // Check if user already reacted
        Optional<MessageReaction> existingReaction = messageReactionRepo.findByMessageAndUser(message, user);
        if (existingReaction.isPresent()) {
            // Update existing reaction
            MessageReaction reaction = existingReaction.get();
            reaction.setEmoji(request.getEmoji());
            MessageReaction savedReaction = messageReactionRepo.save(reaction);
            return MessageReactionResponse.fromEntity(savedReaction);
        } else {
            // Create new reaction
            MessageReaction reaction = new MessageReaction();
            reaction.setMessage(message);
            reaction.setUser(user);
            reaction.setEmoji(request.getEmoji());
            MessageReaction savedReaction = messageReactionRepo.save(reaction);
            return MessageReactionResponse.fromEntity(savedReaction);
        }
    }
    
    @Override
    @Transactional
    public void removeReaction(UUID messageId, User user) throws ChatException {
        Optional<Message> messageOptional = messageRepo.findById(messageId);
        if (messageOptional.isEmpty()) {
            throw new ChatException("Message not found with id: " + messageId);
        }
        
        Message message = messageOptional.get();
        messageReactionRepo.deleteByMessageAndUser(message, user);
    }
    
    @Override
    public List<MessageReactionResponse> getMessageReactions(UUID messageId) throws ChatException {
        Optional<Message> messageOptional = messageRepo.findById(messageId);
        if (messageOptional.isEmpty()) {
            throw new ChatException("Message not found with id: " + messageId);
        }
        
        List<MessageReaction> reactions = messageReactionRepo.findByMessageId(messageId);
        return reactions.stream()
                .map(MessageReactionResponse::fromEntity)
                .collect(Collectors.toList());
    }
    
    // Message reads
    @Override
    @Transactional
    public MessageReadResponse markAsRead(MessageReadRequest request, User user) throws ChatException {
        Optional<Message> messageOptional = messageRepo.findById(request.getMessageId());
        if (messageOptional.isEmpty()) {
            throw new ChatException("Message not found with id: " + request.getMessageId());
        }
        
        Message message = messageOptional.get();
        
        // Check if user is part of the chat
        if (!message.getChat().getUsers().contains(user)) {
            throw new ChatException("User is not part of this chat");
        }
        
        // Check if already read
        Optional<MessageRead> existingRead = messageReadRepo.findByMessageAndUser(message, user);
        if (existingRead.isPresent()) {
            return MessageReadResponse.fromEntity(existingRead.get());
        }
        
        // Create new read record
        MessageRead messageRead = new MessageRead();
        messageRead.setMessage(message);
        messageRead.setUser(user);
        MessageRead savedRead = messageReadRepo.save(messageRead);
        return MessageReadResponse.fromEntity(savedRead);
    }
    
    @Override
    public List<MessageReadResponse> getMessageReads(UUID messageId) throws ChatException {
        Optional<Message> messageOptional = messageRepo.findById(messageId);
        if (messageOptional.isEmpty()) {
            throw new ChatException("Message not found with id: " + messageId);
        }
        
        List<MessageRead> reads = messageReadRepo.findByMessageId(messageId);
        return reads.stream()
                .map(MessageReadResponse::fromEntity)
                .collect(Collectors.toList());
    }
    
    @Override
    public long getUnreadMessageCount(UUID chatId, User user) throws ChatException {
        Chat chat = getChatById(chatId);
        
        // Check if user is part of the chat
        if (!chat.getUsers().contains(user)) {
            throw new ChatException("User is not part of this chat");
        }
        
        return messageReadRepo.countUnreadMessagesForUserInChat(user.getId(), chatId);
    }
    
    @Override
    public List<Message> getUnreadMessages(UUID chatId, User user) throws ChatException {
        Chat chat = getChatById(chatId);
        
        // Check if user is part of the chat
        if (!chat.getUsers().contains(user)) {
            throw new ChatException("User is not part of this chat");
        }
        
        return messageReadRepo.findUnreadMessagesForUserInChat(user.getId(), chatId);
    }
}
