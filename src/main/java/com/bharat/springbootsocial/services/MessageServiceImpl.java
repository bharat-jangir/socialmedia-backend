package com.bharat.springbootsocial.services;

import com.bharat.springbootsocial.entity.Chat;
import com.bharat.springbootsocial.entity.Message;
import com.bharat.springbootsocial.entity.User;
import com.bharat.springbootsocial.repository.ChatRepo;
import com.bharat.springbootsocial.repository.MessageRepo;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class MessageServiceImpl implements MessageServices{
    @Autowired
    private MessageRepo messageRepo;
    @Autowired
    private ChatService chatServices;
    @Autowired
    private ChatRepo chatRepo;
    @Override
    public Message createMessage(User reqUser, UUID chatId, Message req) throws Exception {

        Chat chat = chatServices.getChatById(chatId);
        Message message = new Message();

        message.setContent(req.getContent());
        message.setImage(req.getImage());
        message.setUser(reqUser);
        message.setChat(chat);
        message.setTimestamp(LocalDateTime.now());
        Message savedMessage = messageRepo.save(message);
        chat.getMessages().add(savedMessage);
        chatRepo.save(chat);
        return savedMessage;
    }

    @Override
    public List<Message> findChatsMessages(UUID chatId) throws Exception {
        Chat chat = chatServices.getChatById(chatId);
        return messageRepo.findByChatId(chatId);
    }
}
