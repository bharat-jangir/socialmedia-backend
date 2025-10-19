package com.bharat.springbootsocial.services;

import com.bharat.springbootsocial.entity.GroupMessage;
import com.bharat.springbootsocial.entity.User;
import java.util.UUID;
import com.bharat.springbootsocial.response.PaginatedResponse;

import java.util.List;

public interface GroupMessageService {
    
    // Message CRUD operations
    GroupMessage sendMessage(UUID groupId, User sender, String content, GroupMessage.MessageType messageType);
    GroupMessage sendMessageWithMedia(UUID groupId, User sender, String content, String mediaUrl, GroupMessage.MessageType messageType);
    GroupMessage sendReplyMessage(UUID groupId, User sender, String content, UUID replyToMessageId);
    GroupMessage editMessage(UUID messageId, User user, String newContent);
    void deleteMessage(UUID messageId, User user);
    GroupMessage getMessageById(UUID messageId);
    
    // Message queries
    List<GroupMessage> getGroupMessages(UUID groupId);
    PaginatedResponse<GroupMessage> getGroupMessagesPaginated(UUID groupId, int page, int size);
    List<GroupMessage> getGroupMessagesByType(UUID groupId, GroupMessage.MessageType messageType);
    PaginatedResponse<GroupMessage> getGroupMessagesByTypePaginated(UUID groupId, GroupMessage.MessageType messageType, int page, int size);
    List<GroupMessage> getGroupMediaMessages(UUID groupId);
    PaginatedResponse<GroupMessage> getGroupMediaMessagesPaginated(UUID groupId, int page, int size);
    List<GroupMessage> getGroupSystemMessages(UUID groupId);
    List<GroupMessage> getMessageReplies(UUID messageId);
    
    // Message search
    List<GroupMessage> searchMessagesInGroup(UUID groupId, String query);
    PaginatedResponse<GroupMessage> searchMessagesInGroupPaginated(UUID groupId, String query, int page, int size);
    
    // Message reactions
    GroupMessage addReaction(UUID messageId, User user, String emoji);
    void removeReaction(UUID messageId, User user, String emoji);
    void removeAllReactions(UUID messageId, User user);
    List<GroupMessage> getMessagesWithReactions(UUID groupId);
    int getMessageReactionCount(UUID messageId);
    int getMessageReactionCountByEmoji(UUID messageId, String emoji);
    boolean hasUserReacted(UUID messageId, UUID userId);
    boolean hasUserReactedWithEmoji(UUID messageId, UUID userId, String emoji);
    
    // Message read status
    void markMessageAsRead(UUID messageId, User user);
    void markAllMessagesAsRead(UUID groupId, User user);
    List<GroupMessage> getUnreadMessages(UUID groupId, User user);
    int getUnreadMessageCount(UUID groupId, User user);
    boolean hasUserReadMessage(UUID messageId, UUID userId);
    
    // System messages
    GroupMessage createSystemMessage(UUID groupId, String content, GroupMessage.MessageType messageType);
    GroupMessage createMemberJoinedMessage(UUID groupId, User user);
    GroupMessage createMemberLeftMessage(UUID groupId, User user);
    GroupMessage createCallStartedMessage(UUID groupId, User user, String callType);
    GroupMessage createCallEndedMessage(UUID groupId, User user, String callType, Long duration);
    GroupMessage createGroupCreatedMessage(UUID groupId, User creator);
    GroupMessage createGroupUpdatedMessage(UUID groupId, User user, String updateType);
    
    // Message statistics
    int getGroupMessageCount(UUID groupId);
    int getUserMessageCountInGroup(UUID groupId, UUID userId);
    GroupMessage getLatestMessage(UUID groupId);
    List<GroupMessage> getRecentMessages(UUID groupId, int limit);
    
    // Message validation
    boolean canUserSendMessage(UUID groupId, UUID userId);
    boolean canUserEditMessage(UUID messageId, UUID userId);
    boolean canUserDeleteMessage(UUID messageId, UUID userId);
    boolean canUserReactToMessage(UUID messageId, UUID userId);
    
    // Message moderation
    void muteUserInGroup(UUID groupId, User admin, UUID userId, boolean isMuted);
    boolean isUserMutedInGroup(UUID groupId, UUID userId);
    List<User> getMutedUsersInGroup(UUID groupId);
}

