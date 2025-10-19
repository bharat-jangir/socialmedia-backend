package com.bharat.springbootsocial.repository;

import com.bharat.springbootsocial.entity.Chat;
import com.bharat.springbootsocial.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ChatRepo extends JpaRepository<Chat,UUID> {
    List<Chat> findByUsersId(UUID id);
    @Query("select c from Chat c where :user member of c.users and :reqUser member of c.users")
    Chat findChatByUsersId(@Param("reqUser") User reqUser,@Param("user") User user);
}
