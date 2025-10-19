package com.bharat.springbootsocial.services;

import com.bharat.springbootsocial.entity.Message;
import com.bharat.springbootsocial.entity.User;

import java.util.List;
import java.util.UUID;

public interface MessageServices {
    Message createMessage(User reqUser, UUID chatId, Message req) throws Exception;
    List<Message> findChatsMessages(UUID chatId) throws Exception;
}
