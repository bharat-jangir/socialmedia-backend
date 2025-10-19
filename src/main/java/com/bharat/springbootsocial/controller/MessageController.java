package com.bharat.springbootsocial.controller;

import com.bharat.springbootsocial.entity.Message;
import com.bharat.springbootsocial.entity.User;
import java.util.UUID;
import com.bharat.springbootsocial.services.MessageServices;
import com.bharat.springbootsocial.services.ServiceInt;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@NoArgsConstructor
@Data
@RequestMapping("/api/messages")
public class MessageController {
    @Autowired
    private MessageServices messageService;
    @Autowired
    private ServiceInt userService;

    @PostMapping("/chat/{chatId}")
    public Message createMessage(@RequestHeader("Authorization") String token, @PathVariable UUID chatId, @RequestBody Message req) throws Exception {
        User reqUser = userService.getUserFromToken(token);
        return messageService.createMessage(reqUser, chatId, req);
    }

    @GetMapping("/chat/{chatId}")
    public List<Message> getChatMessages(@PathVariable UUID chatId) throws Exception {
        return messageService.findChatsMessages(chatId);
    }
}
