package com.bharat.springbootsocial.services;

import com.bharat.springbootsocial.entity.Group;
import com.bharat.springbootsocial.entity.GroupMember;
import com.bharat.springbootsocial.entity.GroupMessage;
import com.bharat.springbootsocial.entity.GroupMessageReaction;
import com.bharat.springbootsocial.entity.GroupMessageRead;
import com.bharat.springbootsocial.entity.User;
import com.bharat.springbootsocial.repository.GroupMessageReactionRepo;
import com.bharat.springbootsocial.repository.GroupMessageReadRepo;
import com.bharat.springbootsocial.repository.GroupMessageRepo;
import com.bharat.springbootsocial.repository.GroupRepo;
import com.bharat.springbootsocial.response.PaginatedResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class GroupMessageServiceImpl implements GroupMessageService {
    
    @Autowired
    private GroupMessageRepo groupMessageRepo;
    
    @Autowired
    private GroupMessageReactionRepo groupMessageReactionRepo;
    
    @Autowired
    private GroupMessageReadRepo groupMessageReadRepo;
    
    @Autowired
    private GroupRepo groupRepo;
    
    
    @Autowired
    private GroupService groupService;
    
    @Override
    public GroupMessage sendMessage(UUID groupId, User sender, String content, GroupMessage.MessageType messageType) {
        Group group = groupRepo.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));
        
        if (!canUserSendMessage(groupId, sender.getId())) {
            throw new IllegalArgumentException("You cannot send messages to this group");
        }
        
        if (isUserMutedInGroup(groupId, sender.getId())) {
            throw new IllegalArgumentException("You are muted in this group");
        }
        
        GroupMessage message = new GroupMessage();
        message.setContent(content);
        message.setMessageType(messageType != null ? messageType : GroupMessage.MessageType.TEXT);
        message.setSender(sender);
        message.setGroup(group);
        message.setCreatedAt(LocalDateTime.now());
        
        GroupMessage savedMessage = groupMessageRepo.save(message);
        
        // Update group last activity
        groupService.updateGroupLastActivity(groupId);
        
        return savedMessage;
    }
    
    @Override
    public GroupMessage sendMessageWithMedia(UUID groupId, User sender, String content, String mediaUrl, GroupMessage.MessageType messageType) {
        Group group = groupRepo.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));
        
        if (!canUserSendMessage(groupId, sender.getId())) {
            throw new IllegalArgumentException("You cannot send messages to this group");
        }
        
        if (isUserMutedInGroup(groupId, sender.getId())) {
            throw new IllegalArgumentException("You are muted in this group");
        }
        
        GroupMessage message = new GroupMessage();
        message.setContent(content);
        message.setMessageType(messageType);
        message.setSender(sender);
        message.setGroup(group);
        message.setCreatedAt(LocalDateTime.now());
        
        // Set media URL based on message type
        switch (messageType) {
            case IMAGE:
                message.setImageUrl(mediaUrl);
                break;
            case VIDEO:
                message.setVideoUrl(mediaUrl);
                break;
            case FILE:
            case AUDIO:
                message.setFileUrl(mediaUrl);
                break;
            default:
                throw new IllegalArgumentException("Invalid message type for media");
        }
        
        GroupMessage savedMessage = groupMessageRepo.save(message);
        
        // Update group last activity
        groupService.updateGroupLastActivity(groupId);
        
        return savedMessage;
    }
    
    @Override
    public GroupMessage sendReplyMessage(UUID groupId, User sender, String content, UUID replyToMessageId) {
        GroupMessage replyToMessage = getMessageById(replyToMessageId);
        
        if (!replyToMessage.getGroup().getId().equals(groupId)) {
            throw new IllegalArgumentException("Reply message must be from the same group");
        }
        
        GroupMessage message = new GroupMessage();
        message.setContent(content);
        message.setMessageType(GroupMessage.MessageType.TEXT);
        message.setSender(sender);
        message.setGroup(replyToMessage.getGroup());
        message.setReplyTo(replyToMessage);
        message.setCreatedAt(LocalDateTime.now());
        
        GroupMessage savedMessage = groupMessageRepo.save(message);
        
        // Update group last activity
        groupService.updateGroupLastActivity(groupId);
        
        return savedMessage;
    }
    
    @Override
    public GroupMessage editMessage(UUID messageId, User user, String newContent) {
        GroupMessage message = getMessageById(messageId);
        
        if (!canUserEditMessage(messageId, user.getId())) {
            throw new IllegalArgumentException("You cannot edit this message");
        }
        
        message.editMessage(newContent);
        return groupMessageRepo.save(message);
    }
    
    @Override
    public void deleteMessage(UUID messageId, User user) {
        GroupMessage message = getMessageById(messageId);
        
        if (!canUserDeleteMessage(messageId, user.getId())) {
            throw new IllegalArgumentException("You cannot delete this message");
        }
        
        message.deleteMessage();
        groupMessageRepo.save(message);
    }
    
    @Override
    public GroupMessage getMessageById(UUID messageId) {
        return groupMessageRepo.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Message not found with id: " + messageId));
    }
    
    @Override
    public List<GroupMessage> getGroupMessages(UUID groupId) {
        return groupMessageRepo.findMessagesByGroupId(groupId);
    }
    
    @Override
    public PaginatedResponse<GroupMessage> getGroupMessagesPaginated(UUID groupId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<GroupMessage> messagesPage = groupMessageRepo.findMessagesByGroupIdPaginated(groupId, pageable);
        
        return new PaginatedResponse<>(
                messagesPage.getContent(),
                messagesPage.getNumber(),
                messagesPage.getSize(),
                messagesPage.getTotalElements(),
                messagesPage.getTotalPages(),
                messagesPage.hasNext(),
                messagesPage.hasPrevious(),
                messagesPage.isFirst(),
                messagesPage.isLast()
        );
    }
    
    @Override
    public List<GroupMessage> getGroupMessagesByType(UUID groupId, GroupMessage.MessageType messageType) {
        return groupMessageRepo.findMessagesByGroupIdAndType(groupId, messageType);
    }
    
    @Override
    public PaginatedResponse<GroupMessage> getGroupMessagesByTypePaginated(UUID groupId, GroupMessage.MessageType messageType, int page, int size) {
        // This would need a custom query in the repository
        // For now, we'll filter in memory (not ideal for large datasets)
        List<GroupMessage> allMessages = getGroupMessagesByType(groupId, messageType);
        
        int startIndex = page * size;
        int endIndex = Math.min(startIndex + size, allMessages.size());
        
        if (startIndex >= allMessages.size()) {
            startIndex = allMessages.size();
        }
        
        List<GroupMessage> paginatedMessages = allMessages.subList(startIndex, endIndex);
        int totalPages = (int) Math.ceil((double) allMessages.size() / size);
        
        return new PaginatedResponse<>(
                paginatedMessages,
                page,
                size,
                (long) allMessages.size(),
                totalPages,
                page < totalPages - 1,
                page > 0,
                page == 0,
                page == totalPages - 1
        );
    }
    
    @Override
    public List<GroupMessage> getGroupMediaMessages(UUID groupId) {
        return groupMessageRepo.findMediaMessagesByGroupId(groupId);
    }
    
    @Override
    public PaginatedResponse<GroupMessage> getGroupMediaMessagesPaginated(UUID groupId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<GroupMessage> messagesPage = groupMessageRepo.findMediaMessagesByGroupIdPaginated(groupId, pageable);
        
        return new PaginatedResponse<>(
                messagesPage.getContent(),
                messagesPage.getNumber(),
                messagesPage.getSize(),
                messagesPage.getTotalElements(),
                messagesPage.getTotalPages(),
                messagesPage.hasNext(),
                messagesPage.hasPrevious(),
                messagesPage.isFirst(),
                messagesPage.isLast()
        );
    }
    
    @Override
    public List<GroupMessage> getGroupSystemMessages(UUID groupId) {
        return groupMessageRepo.findSystemMessagesByGroupId(groupId);
    }
    
    @Override
    public List<GroupMessage> getMessageReplies(UUID messageId) {
        return groupMessageRepo.findRepliesByMessageId(messageId);
    }
    
    @Override
    public List<GroupMessage> searchMessagesInGroup(UUID groupId, String query) {
        return groupMessageRepo.searchMessagesByContent(groupId, query);
    }
    
    @Override
    public PaginatedResponse<GroupMessage> searchMessagesInGroupPaginated(UUID groupId, String query, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<GroupMessage> messagesPage = groupMessageRepo.searchMessagesByContentPaginated(groupId, query, pageable);
        
        return new PaginatedResponse<>(
                messagesPage.getContent(),
                messagesPage.getNumber(),
                messagesPage.getSize(),
                messagesPage.getTotalElements(),
                messagesPage.getTotalPages(),
                messagesPage.hasNext(),
                messagesPage.hasPrevious(),
                messagesPage.isFirst(),
                messagesPage.isLast()
        );
    }
    
    @Override
    public GroupMessage addReaction(UUID messageId, User user, String emoji) {
        GroupMessage message = getMessageById(messageId);
        
        if (!canUserReactToMessage(messageId, user.getId())) {
            throw new IllegalArgumentException("You cannot react to this message");
        }
        
        // Check if user already reacted with this emoji
        if (hasUserReactedWithEmoji(messageId, user.getId(), emoji)) {
            throw new IllegalArgumentException("You have already reacted with this emoji");
        }
        
        GroupMessageReaction reaction = new GroupMessageReaction();
        reaction.setMessage(message);
        reaction.setUser(user);
        reaction.setEmoji(emoji);
        reaction.setCreatedAt(LocalDateTime.now());
        
        groupMessageReactionRepo.save(reaction);
        
        return message;
    }
    
    @Override
    public void removeReaction(UUID messageId, User user, String emoji) {
        Optional<GroupMessageReaction> reaction = groupMessageReactionRepo.findReactionByMessageIdAndUserIdAndEmoji(messageId, user.getId(), emoji);
        
        if (reaction.isPresent()) {
            groupMessageReactionRepo.delete(reaction.get());
        } else {
            throw new IllegalArgumentException("Reaction not found or you don't have permission to remove it");
        }
    }
    
    @Override
    public void removeAllReactions(UUID messageId, User user) {
        List<GroupMessageReaction> reactions = groupMessageReactionRepo.findReactionsByUserId(user.getId()).stream()
                .filter(reaction -> reaction.getMessage().getId().equals(messageId))
                .toList();
        groupMessageReactionRepo.deleteAll(reactions);
    }
    
    @Override
    public List<GroupMessage> getMessagesWithReactions(UUID groupId) {
        // This would need a custom query to find messages that have reactions
        // For now, return all messages and filter in service layer
        return getGroupMessages(groupId).stream()
                .filter(message -> message.getReactionCount() > 0)
                .toList();
    }
    
    @Override
    public int getMessageReactionCount(UUID messageId) {
        return groupMessageReactionRepo.countReactionsByMessageId(messageId).intValue();
    }
    
    @Override
    public int getMessageReactionCountByEmoji(UUID messageId, String emoji) {
        return groupMessageReactionRepo.countReactionsByMessageIdAndEmoji(messageId, emoji).intValue();
    }
    
    @Override
    public boolean hasUserReacted(UUID messageId, UUID userId) {
        return groupMessageReactionRepo.hasUserReactedToMessage(messageId, userId);
    }
    
    @Override
    public boolean hasUserReactedWithEmoji(UUID messageId, UUID userId, String emoji) {
        return groupMessageReactionRepo.hasUserReactedWithEmoji(messageId, userId, emoji);
    }
    
    @Override
    public void markMessageAsRead(UUID messageId, User user) {
        if (!hasUserReadMessage(messageId, user.getId())) {
            GroupMessageRead read = new GroupMessageRead();
            read.setMessage(getMessageById(messageId));
            read.setUser(user);
            read.setReadAt(LocalDateTime.now());
            groupMessageReadRepo.save(read);
        }
    }
    
    @Override
    public void markAllMessagesAsRead(UUID groupId, User user) {
        List<GroupMessage> unreadMessages = getUnreadMessages(groupId, user);
        for (GroupMessage message : unreadMessages) {
            markMessageAsRead(message.getId(), user);
        }
    }
    
    @Override
    public List<GroupMessage> getUnreadMessages(UUID groupId, User user) {
        return groupMessageReadRepo.findUnreadMessagesForUserInGroup(groupId, user.getId());
    }
    
    @Override
    public int getUnreadMessageCount(UUID groupId, User user) {
        return groupMessageReadRepo.countUnreadMessagesForUserInGroup(groupId, user.getId()).intValue();
    }
    
    @Override
    public boolean hasUserReadMessage(UUID messageId, UUID userId) {
        return groupMessageReadRepo.hasUserReadMessage(messageId, userId);
    }
    
    @Override
    public GroupMessage createSystemMessage(UUID groupId, String content, GroupMessage.MessageType messageType) {
        Group group = groupRepo.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));
        
        GroupMessage message = new GroupMessage();
        message.setContent(content);
        message.setMessageType(messageType);
        message.setSender(null); // System messages don't have a sender
        message.setGroup(group);
        message.setCreatedAt(LocalDateTime.now());
        
        return groupMessageRepo.save(message);
    }
    
    @Override
    public GroupMessage createMemberJoinedMessage(UUID groupId, User user) {
        String content = user.getFname() + " " + user.getLname() + " joined the group";
        return createSystemMessage(groupId, content, GroupMessage.MessageType.MEMBER_JOINED);
    }
    
    @Override
    public GroupMessage createMemberLeftMessage(UUID groupId, User user) {
        String content = user.getFname() + " " + user.getLname() + " left the group";
        return createSystemMessage(groupId, content, GroupMessage.MessageType.MEMBER_LEFT);
    }
    
    @Override
    public GroupMessage createCallStartedMessage(UUID groupId, User user, String callType) {
        String content = user.getFname() + " " + user.getLname() + " started a " + callType + " call";
        return createSystemMessage(groupId, content, GroupMessage.MessageType.CALL_START);
    }
    
    @Override
    public GroupMessage createCallEndedMessage(UUID groupId, User user, String callType, Long duration) {
        String content = user.getFname() + " " + user.getLname() + " ended the " + callType + " call (Duration: " + duration + "s)";
        return createSystemMessage(groupId, content, GroupMessage.MessageType.CALL_END);
    }
    
    @Override
    public GroupMessage createGroupCreatedMessage(UUID groupId, User creator) {
        String content = creator.getFname() + " " + creator.getLname() + " created the group";
        return createSystemMessage(groupId, content, GroupMessage.MessageType.GROUP_CREATED);
    }
    
    @Override
    public GroupMessage createGroupUpdatedMessage(UUID groupId, User user, String updateType) {
        String content = user.getFname() + " " + user.getLname() + " updated the group: " + updateType;
        return createSystemMessage(groupId, content, GroupMessage.MessageType.GROUP_UPDATED);
    }
    
    @Override
    public int getGroupMessageCount(UUID groupId) {
        return groupMessageRepo.countMessagesByGroupId(groupId).intValue();
    }
    
    @Override
    public int getUserMessageCountInGroup(UUID groupId, UUID userId) {
        return groupMessageRepo.countMessagesBySenderId(userId).intValue();
    }
    
    @Override
    public GroupMessage getLatestMessage(UUID groupId) {
        return groupMessageRepo.findLatestMessageByGroupId(groupId).orElse(null);
    }
    
    @Override
    public List<GroupMessage> getRecentMessages(UUID groupId, int limit) {
        return getGroupMessages(groupId).stream()
                .limit(limit)
                .toList();
    }
    
    @Override
    public boolean canUserSendMessage(UUID groupId, UUID userId) {
        return groupService.isUserMember(groupId, userId);
    }
    
    @Override
    public boolean canUserEditMessage(UUID messageId, UUID userId) {
        GroupMessage message = getMessageById(messageId);
        return message.getSender().getId().equals(userId);
    }
    
    @Override
    public boolean canUserDeleteMessage(UUID messageId, UUID userId) {
        GroupMessage message = getMessageById(messageId);
        Group group = message.getGroup();
        
        // User can delete their own message or admins can delete any message
        return message.getSender().getId().equals(userId) || 
               groupService.isUserAdmin(group.getId(), userId);
    }
    
    @Override
    public boolean canUserReactToMessage(UUID messageId, UUID userId) {
        GroupMessage message = getMessageById(messageId);
        return groupService.isUserMember(message.getGroup().getId(), userId);
    }
    
    @Override
    public void muteUserInGroup(UUID groupId, User admin, UUID userId, boolean isMuted) {
        groupService.muteMember(groupId, admin, userId, isMuted);
    }
    
    @Override
    public boolean isUserMutedInGroup(UUID groupId, UUID userId) {
        try {
            GroupMember member = groupService.getGroupMember(groupId, userId);
            return member.getIsMuted();
        } catch (Exception e) {
            return false;
        }
    }
    
    @Override
    public List<User> getMutedUsersInGroup(UUID groupId) {
        return groupService.getGroupMembers(groupId).stream()
                .filter(GroupMember::getIsMuted)
                .map(GroupMember::getUser)
                .toList();
    }
}
